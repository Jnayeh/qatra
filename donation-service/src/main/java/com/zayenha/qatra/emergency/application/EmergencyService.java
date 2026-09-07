package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.domain.GeoUtils;
import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.port.out.AppointmentServiceProvider;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra._shared.domain.enums.CenterStatus;
import com.zayenha.qatra.emergency.application.proxy.EmergencyCenterProxy;
import com.zayenha.qatra.emergency.application.proxy.EmergencyDonorProxy;
import com.zayenha.qatra.emergency.domain.exception.EmergencyErrorCode;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class EmergencyService implements EmergencyCommandUseCases, EmergencyQueryUseCases {

    private final AppointmentServiceProvider appointmentApi;
    private final EmergencyRepositoryPort repository;
    private final EmergencyDonorProxy donorProxy;
    private final EmergencyCenterProxy centerProxy;
    private final CacheService cacheService;
    private final MatchingService matchingService;

    @Value("${emergency.expiration-minutes:180}")
    private long expirationHours;
    private final AuditPublisher auditPublisher;

    @Value("${emergency.escalation-radius-increment:10}")
    private int escalationRadiusIncrement;

    @Override
    @Transactional
    public EmergencyRequest create(Long centerId, Long createdByStaffId, BloodType bloodType, Integer unitsNeeded,
                                    EmergencyUrgency urgency, Integer matchRadius, String contactPhone) {
        var center = centerProxy.findCenterById(centerId)
            .orElseThrow(() -> new NotFoundException("Center not found: " + centerId,
                    EmergencyErrorCode.CENTER_NOT_ACTIVE.name()));
        if (center.getStatus() != CenterStatus.ACTIVE) {
            throw new ValidationException("Only active centers can submit emergency requests",
                    EmergencyErrorCode.CENTER_NOT_ACTIVE.name());
        }
        if (!centerProxy.isStaffOrAdminOfCenter(createdByStaffId, centerId)) {
            throw new ValidationException("User is not a member of this center",
                    EmergencyErrorCode.NOT_CENTER_MEMBER.name());
        }
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
    public EmergencyRequest escalate(Long id) {
        var request = findOrThrow(id);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Only open emergencies can be escalated",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        var newLevel = request.getEscalationLevel() != null ? request.getEscalationLevel() + 1 : 1;
        var newRadius = (request.getMatchRadius() != null ? request.getMatchRadius() : 0) + escalationRadiusIncrement;
        request.setEscalationLevel(newLevel);
        request.setMatchRadius(newRadius);
        var saved = repository.save(request);
        cacheService.evictByPattern("emergencies:*");
        auditPublisher.publish("EMERGENCY_ESCALATED", saved.getId(), "EmergencyRequest", null,
            Map.of("escalationLevel", newLevel, "matchRadius", newRadius));
        matchingService.matchDonors(saved);
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse acceptResponse(Long emergencyId, Long userId, Long slotId) {
        var emergency = validateEmergency(emergencyId, userId);
        var acceptedResponses = findResponsesByEmergencyId(emergencyId)
                .stream().filter(r -> ResponseStatus.ACCEPTED.equals(r.getStatus())).count();
        if (emergency.getUnitsNeeded() <= acceptedResponses) {
            fulfillEmergency(emergency);
            return null;
        }
        var donor = donorProxy.findByUserId(userId);
        var response = new DonorResponse(emergencyId, userId);
        response.accept(slotId);
        var saved = repository.saveResponse(response);
        var newAcceptedCount = acceptedResponses +1;

        repository.findMatchResultByEmergencyIdAndDonorId(
            response.getEmergencyId(), response.getDonorId()).ifPresent(mr -> {
            mr.setStatus(MatchStatus.RESPONDED);
            repository.saveMatchResult(mr);
        });

        donor.setConsecutiveEmergencyDeclines(0);
        donor.setLastAcceptAt(Instant.now());
        donor.setUpdatedAt(Instant.now());
        donorProxy.saveDonor(donor);
        cacheService.evictByPattern("donorProfiles:*");

        appointmentApi.book(userId, slotId, emergencyId, AppointmentType.EMERGENCY);
        if (emergency.getUnitsNeeded() == newAcceptedCount) fulfillEmergency(emergency);
        cacheService.evictByPattern("responses:*");
        auditPublisher.publish("RESPONSE_ACCEPTED", response.getId(), "EmergencyRequest",
            null,
            Map.of("emergencyId", emergencyId, "userId", userId, "status", "ACCEPTED", "slotId", slotId));
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse declineResponse(Long emergencyId, Long userId, String reason) {
        validateEmergency(emergencyId, userId);
        var response = new DonorResponse(emergencyId, userId);
        response.decline(reason);
        var saved = repository.saveResponse(response);
        var profile = donorProxy.findByUserId(userId);

        repository.findMatchResultByEmergencyIdAndDonorId(
            response.getEmergencyId(), response.getDonorId()).ifPresent(mr -> {
            mr.setStatus(MatchStatus.RESPONDED);
            repository.saveMatchResult(mr);
        });


        var declines = profile.getConsecutiveEmergencyDeclines() != null
            ? profile.getConsecutiveEmergencyDeclines() + 1 : 1;
        profile.setConsecutiveEmergencyDeclines(declines);
        if (declines >= 3) {
            profile.setFlaggedForManualReview(true);
        }
        donorProxy.saveDonor(profile);
        cacheService.evictByPattern("donorProfiles:*");


        cacheService.evictByPattern("responses:*");
        auditPublisher.publish("RESPONSE_DECLINED", emergencyId, "EmergencyRequest",
            null,
            Map.of("emergencyId", emergencyId, "userId", userId, "status", "DECLINED", "reason", reason));
        return saved;
    }

    @Override
    @Transactional
    public EmergencyRequest resolve(Long id, Long resolvedByUserId) {
        var request = findOrThrow(id);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Only open emergencies can be resolved",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        request.resolve(resolvedByUserId);
        var saved = repository.save(request);
        cacheService.evictByPattern("emergencies:*");
        auditPublisher.publish("EMERGENCY_RESOLVED", saved.getId(), "EmergencyRequest",
            Map.of("status", EmergencyStatus.OPEN.name()),
            Map.of("status", EmergencyStatus.FULFILLED.name(), "resolvedByUserId", resolvedByUserId));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyRequest findById(Long id) {
        var key = "emergencies:" + id;
        var cached = cacheService.get(key, EmergencyRequest.class);
        if (cached.isPresent()) return cached.get();
        var result = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emergency not found: " + id,
                        EmergencyErrorCode.EMERGENCY_NOT_FOUND.name()));
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria, Long centerId, EmergencyStatus status) {
        return repository.findAll(criteria, centerId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenByBloodType(BloodType bloodType) {
        return repository.findByBloodTypeAndStatus(bloodType, EmergencyStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenWithinRadius(double latitude, double longitude, double radiusKm) {
        return repository.findOpenByStatus().stream()
                .filter(e -> {
                    var center = centerProxy.findCenterById(e.getCenterId()).orElse(null);
                    return center != null && center.getLatitude() != null && center.getLongitude() != null
                        && GeoUtils.distanceKm(latitude, longitude, center.getLatitude(), center.getLongitude()) <= radiusKm;
                })
                .toList();
    }

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public MatchResultStats getMatchStats(Long emergencyId) {
        return repository.countMatchStatsByEmergencyId(emergencyId);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseStats getResponseStats(Long emergencyId) {
        return repository.countResponseStatsByEmergencyId(emergencyId);
    }

    private EmergencyRequest validateEmergency(Long emergencyId, Long donorId) {
        var emergency = findOrThrow(emergencyId);
        if (emergency.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Emergency is no longer open",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        if (repository.existsByEmergencyIdAndDonorId(emergencyId, donorId)) {
            throw new ConflictException("Donor already responded to this emergency",
                    EmergencyErrorCode.RESPONSE_ALREADY_EXISTS.name());
        }
        return emergency;
    }

    private void fulfillEmergency(EmergencyRequest emergency) {
        cacheService.evictByPattern("emergencies:*");
        emergency.fulfill();
        repository.save(emergency);
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
