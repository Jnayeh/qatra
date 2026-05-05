package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.emergency.application.proxy.EmergencyDonorProxy;
import com.zayenha.qatra.emergency.domain.exception.EmergencyErrorCode;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmergencyService implements EmergencyCommandUseCases, EmergencyQueryUseCases {

    private final EmergencyRepositoryPort repository;
    private final EmergencyDonorProxy donorProxy;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;
    private final MatchingService matchingService;

    @Value("${emergency.expiration-minutes:180}")
    private long expirationHours;
    private final AuditPublisher auditPublisher;

    @Override
    @Transactional
    public EmergencyRequest create(Long centerId, Long createdByStaffId, BloodType bloodType, Integer unitsNeeded,
                                    EmergencyUrgency urgency, Integer matchRadius, String contactPhone) {
        var request = new EmergencyRequest(centerId, createdByStaffId, bloodType, unitsNeeded, urgency, contactPhone, matchRadius, Instant.now().plus(expirationHours, ChronoUnit.MINUTES));
        var saved = repository.save(request);
        cacheService.evictByPattern("emergencies:*");
        auditPublisher.publish("EMERGENCY_CREATED", saved.getId(), "EmergencyRequest", null,
            Map.of("centerId", centerId, "bloodType", bloodType.name(),
                   "unitsNeeded", unitsNeeded, "urgency", urgency.name()));
        matchingService.matchDonors(saved);
        return saved;
    }

    @Override
    @Transactional
    public EmergencyRequest update(Long id, Long centerId, BloodType bloodType, Integer unitsNeeded,
                                    EmergencyUrgency urgency, Integer matchRadius, String contactPhone) {
        var request = findOrThrow(id);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Only open emergencies can be updated",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        request.setCenterId(centerId);
        request.setBloodType(bloodType);
        request.setUnitsNeeded(unitsNeeded);
        request.setUrgency(urgency);
        request.setMatchRadius(matchRadius);
        request.setContactPhone(contactPhone);
        var saved = repository.save(request);
        auditPublisher.publish("EMERGENCY_UPDATED", saved.getId(), "EmergencyRequest", null,
            Map.of("centerId", centerId, "bloodType", bloodType.name(), "urgency", urgency.name()));
        return saved;
    }

    @Override
    @Transactional
    public EmergencyRequest cancel(Long id) {
        var request = findOrThrow(id);
        if (request.getStatus() == EmergencyStatus.FULFILLED) {
            throw new ValidationException("Cannot cancel a fulfilled emergency",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        var oldStatus = request.getStatus();
        request.cancel();
        var saved = repository.save(request);
        cacheService.evictByPattern("emergencies:*");
        cacheService.evictByPattern("responses:*");
        auditPublisher.publish("EMERGENCY_CANCELLED", saved.getId(), "EmergencyRequest",
            Map.of("status", oldStatus != null ? oldStatus.name() : null),
            Map.of("status", EmergencyStatus.CANCELLED.name()));
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse respond(Long emergencyId, Long donorId) {
        var request = findOrThrow(emergencyId);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Emergency is no longer open",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        if (repository.existsByEmergencyIdAndDonorId(emergencyId, donorId)) {
            throw new ConflictException("Donor already responded to this emergency",
                    EmergencyErrorCode.RESPONSE_ALREADY_EXISTS.name());
        }
        var response = new DonorResponse(emergencyId, donorId);
        var saved = repository.saveResponse(response);
        cacheService.evictByPattern("responses:*");

        // Sync MatchResult status to RESPONDED
        repository.findMatchResultByEmergencyIdAndDonorId(emergencyId, donorId).ifPresent(mr -> {
            mr.setStatus(MatchStatus.RESPONDED);
            repository.saveMatchResult(mr);
        });

        auditPublisher.publish("DONOR_RESPONDED", saved.getId(), "EmergencyRequest", null,
            Map.of("emergencyId", emergencyId, "donorId", donorId));
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse acceptResponse(Long responseId, Long slotId) {
        var response = findResponseOrThrow(responseId);
        var oldStatus = response.getStatus();
        response.accept(slotId);
        var saved = repository.saveResponse(response);

        // Update MatchResult status
        repository.findMatchResultByEmergencyIdAndDonorId(
            response.getEmergencyId(), response.getDonorId()).ifPresent(mr -> {
            mr.setStatus(MatchStatus.RESPONDED);
            repository.saveMatchResult(mr);
        });

        // Reset consecutive declines on accept
        donorProxy.findByUserId(response.getDonorId()).ifPresent(dto -> {
            dto.setConsecutiveEmergencyDeclines(0);
            dto.setUpdatedAt(Instant.now());
            donorProxy.saveDonor(dto);
            cacheService.evictByPattern("donorProfiles:*");
        });

        // Accept on DonorResponse triggers emergency fulfillment check
        var emergency = findOrThrow(response.getEmergencyId());
        emergency.fulfill();
        repository.save(emergency);
        cacheService.evictByPattern("emergencies:*");
        cacheService.evictByPattern("responses:*");
        auditPublisher.publish("RESPONSE_ACCEPTED", responseId, "EmergencyRequest",
            Map.of("status", oldStatus != null ? oldStatus.name() : null),
            Map.of("status", "ACCEPTED", "slotId", slotId));
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse declineResponse(Long responseId) {
        var response = findResponseOrThrow(responseId);
        var oldStatus = response.getStatus();
        response.decline(null);
        var saved = repository.saveResponse(response);

        // Update MatchResult status
        repository.findMatchResultByEmergencyIdAndDonorId(
            response.getEmergencyId(), response.getDonorId()).ifPresent(mr -> {
            mr.setStatus(MatchStatus.RESPONDED);
            repository.saveMatchResult(mr);
        });

        // Track consecutive declines
        donorProxy.findByUserId(response.getDonorId()).ifPresent(profile -> {
            var declines = profile.getConsecutiveEmergencyDeclines() != null
                ? profile.getConsecutiveEmergencyDeclines() + 1 : 1;
            profile.setConsecutiveEmergencyDeclines(declines);
            if (declines >= 3) {
                profile.setFlaggedForManualReview(true);
            }
            donorProxy.saveDonor(profile);
            cacheService.evictByPattern("donorProfiles:*");
        });

        cacheService.evictByPattern("responses:*");
        auditPublisher.publish("RESPONSE_DECLINED", responseId, "EmergencyRequest",
            Map.of("status", oldStatus != null ? oldStatus.name() : null),
            Map.of("status", "DECLINED"));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmergencyRequest> findById(Long id) {
        var key = "emergencies:" + id;
        var cached = cacheService.get(key, EmergencyRequest.class);
        if (cached.isPresent()) return cached;
        var result = repository.findById(id);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenByBloodType(BloodType bloodType) {
        return repository.findByBloodTypeAndStatus(bloodType, EmergencyStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenWithinRadius(double latitude, double longitude, double radiusKm) {
        return repository.findAll(SearchCriteria.defaultAll()).content().stream()
                .filter(e -> e.getStatus() == EmergencyStatus.OPEN)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> findResponsesByEmergencyId(Long emergencyId) {
        var key = "responses:emergency:" + emergencyId;
        return cacheService.get(key, new TypeReference<List<DonorResponse>>() {})
                .orElseGet(() -> {
                    var result = repository.findResponsesByEmergencyId(emergencyId);
                    cacheService.put(key, result);
                    return result;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> findResponsesByDonorId(Long donorId) {
        var key = "responses:donor:" + donorId;
        return cacheService.get(key, new TypeReference<List<DonorResponse>>() {})
                .orElseGet(() -> {
                    var result = repository.findResponsesByDonorId(donorId);
                    cacheService.put(key, result);
                    return result;
                });
    }

    private EmergencyRequest findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emergency not found: " + id,
                        EmergencyErrorCode.EMERGENCY_NOT_FOUND.name()));
    }

    private DonorResponse findResponseOrThrow(Long id) {
        return repository.findResponseById(id)
                .orElseThrow(() -> new NotFoundException("Response not found: " + id,
                        EmergencyErrorCode.RESPONSE_NOT_FOUND.name()));
    }
}
