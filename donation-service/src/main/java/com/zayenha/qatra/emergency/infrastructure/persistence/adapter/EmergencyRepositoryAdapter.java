package com.zayenha.qatra.emergency.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.application.proxy.EmergencyCenterProxy;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.MatchResult;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.mapper.EmergencyMapper;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.DonorResponseJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.MatchResultJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmergencyRepositoryAdapter implements EmergencyRepositoryPort {

    private final EmergencyJpaRepository emergencyJpaRepository;
    private final DonorResponseJpaRepository responseJpaRepository;
    private final MatchResultJpaRepository matchResultJpaRepository;
    private final EmergencyCenterProxy centerProxy;
    private final EmergencyMapper mapper;
    private final CacheService cacheService;

    @Override
    public EmergencyRequest save(EmergencyRequest request) {
        if (request.getId() != null) {
            var existing = emergencyJpaRepository.findById(request.getId()).orElseThrow();
            merge(existing, request);
            return mapper.toDomain(emergencyJpaRepository.save(existing));
        }
        return mapper.toDomain(emergencyJpaRepository.save(mapper.toEntity(request)));
    }

    private void merge(EmergencyRequestEntity existing, EmergencyRequest source) {
        var updated = mapper.toEntity(source);
        existing.setCenter(updated.getCenter());
        existing.setCreatedByStaff(updated.getCreatedByStaff());
        existing.setBloodType(updated.getBloodType());
        existing.setUnitsNeeded(updated.getUnitsNeeded());
        existing.setUrgency(updated.getUrgency());
        existing.setMatchRadius(updated.getMatchRadius());
        existing.setContactPhone(updated.getContactPhone());
        existing.setStatus(updated.getStatus());
        existing.setExpiresAt(updated.getExpiresAt());
        existing.setEscalationLevel(updated.getEscalationLevel());
        existing.setResolvedAt(updated.getResolvedAt());
        existing.setResolvedBy(updated.getResolvedBy());
    }

    @Override
    public Optional<EmergencyRequest> findById(Long id) {
        return emergencyJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = emergencyJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        var total = cachedCount("count:emergencies");
        return new PageResult<>(
            page.getContent().stream().map(mapper::toDomain).toList(),
            page.getNumber(), page.getSize(),
            total, (int) Math.ceil((double) total / criteria.size())
        );
    }

    private long cachedCount(String key) {
        var cached = cacheService.get(key, Long.class);
        if (cached.isPresent()) return cached.get();
        var count = emergencyJpaRepository.count();
        cacheService.put(key, count, Duration.ofSeconds(6800));
        return count;
    }

    @Override
    public List<EmergencyRequest> findByBloodTypeAndStatus(BloodType bloodType, EmergencyStatus status) {
        return emergencyJpaRepository.findByBloodTypeAndStatus(bloodType, status)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<EmergencyRequest> findByStatus(EmergencyStatus status) {
        return emergencyJpaRepository.findByStatus(status)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<EmergencyRequest> findOpenByStatus() {
        return emergencyJpaRepository.findByStatus(EmergencyStatus.OPEN)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public DonorResponse saveResponse(DonorResponse response) {
        if (response.getId() != null) {
            var existing = responseJpaRepository.findById(response.getId()).orElseThrow();
            existing.setSlot(response.getSlotId() != null ? centerProxy.getSlotReference(response.getSlotId()) : null);
            existing.setStatus(response.getStatus());
            existing.setRespondedAt(response.getRespondedAt());
            return mapper.toResponseDomain(responseJpaRepository.save(existing));
        }
        return mapper.toResponseDomain(responseJpaRepository.save(mapper.toResponseEntity(response)));
    }

    @Override
    public Optional<DonorResponse> findResponseById(Long id) {
        return responseJpaRepository.findById(id).map(mapper::toResponseDomain);
    }

    @Override
    public List<DonorResponse> findResponsesByEmergencyId(Long emergencyId) {
        return responseJpaRepository.findByEmergency_IdOrderByCreatedAtAsc(emergencyId)
                .stream().map(mapper::toResponseDomain).toList();
    }

    @Override
    public List<DonorResponse> findResponsesByDonorId(Long donorId) {
        return responseJpaRepository.findByDonor_IdOrderByCreatedAtDesc(donorId)
                .stream().map(mapper::toResponseDomain).toList();
    }

    @Override
    public boolean existsByEmergencyIdAndDonorId(Long emergencyId, Long donorId) {
        return responseJpaRepository.existsByEmergency_IdAndDonor_Id(emergencyId, donorId);
    }

    @Override
    public MatchResult saveMatchResult(MatchResult matchResult) {
        if (matchResult.getId() != null) {
            var existing = matchResultJpaRepository.findById(matchResult.getId()).orElseThrow();
            existing.setRadius(matchResult.getRadius());
            existing.setBloodType(matchResult.getBloodType());
            existing.setEscalationLevel(matchResult.getEscalationLevel());
            existing.setStatus(matchResult.getStatus());
            existing.setRespondedAt(matchResult.getRespondedAt());
            return mapper.toMatchResultDomain(matchResultJpaRepository.save(existing));
        }
        return mapper.toMatchResultDomain(matchResultJpaRepository.save(mapper.toMatchResultEntity(matchResult)));
    }

    @Override
    public Optional<MatchResult> findMatchResultByEmergencyIdAndDonorId(Long emergencyId, Long donorId) {
        return matchResultJpaRepository.findByEmergency_IdAndDonor_Id(emergencyId, donorId)
                .map(mapper::toMatchResultDomain);
    }

    @Override
    public List<MatchResult> findMatchResultsByEmergencyId(Long emergencyId) {
        return matchResultJpaRepository.findByEmergency_Id(emergencyId)
                .stream().map(mapper::toMatchResultDomain).toList();
    }

    @Override
    public List<MatchResult> findMatchResultsByDonorId(Long donorId) {
        return matchResultJpaRepository.findByDonor_Id(donorId)
                .stream().map(mapper::toMatchResultDomain).toList();
    }
}
