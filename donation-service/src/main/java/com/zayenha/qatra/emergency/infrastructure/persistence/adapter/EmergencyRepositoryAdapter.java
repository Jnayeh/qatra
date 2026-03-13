package com.zayenha.qatra.emergency.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.DonorResponseJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmergencyRepositoryAdapter implements EmergencyRepositoryPort {

    private final EmergencyJpaRepository emergencyJpaRepository;
    private final DonorResponseJpaRepository responseJpaRepository;

    @Override
    public EmergencyRequest save(EmergencyRequest request) {
        var entity = toEntity(request);
        if (entity.getId() != null) {
            var existing = emergencyJpaRepository.findById(entity.getId()).orElseThrow();
            existing.setPatientName(entity.getPatientName());
            existing.setBloodType(entity.getBloodType());
            existing.setUnitsNeeded(entity.getUnitsNeeded());
            existing.setUrgency(entity.getUrgency());
            existing.setHospital(entity.getHospital());
            existing.setHospitalAddress(entity.getHospitalAddress());
            existing.setLatitude(entity.getLatitude());
            existing.setLongitude(entity.getLongitude());
            existing.setContactPhone(entity.getContactPhone());
            existing.setStatus(entity.getStatus());
            existing.setExpiresAt(entity.getExpiresAt());
            return toDomain(emergencyJpaRepository.save(existing));
        }
        return toDomain(emergencyJpaRepository.save(entity));
    }

    @Override
    public Optional<EmergencyRequest> findById(Long id) {
        return emergencyJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = emergencyJpaRepository.findAllByOrderByCreatedAtDesc(pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public List<EmergencyRequest> findByBloodTypeAndStatus(BloodType bloodType, EmergencyStatus status) {
        return emergencyJpaRepository.findByBloodTypeAndStatus(bloodType, status)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public DonorResponse saveResponse(DonorResponse response) {
        var entity = toResponseEntity(response);
        if (entity.getId() != null) {
            var existing = responseJpaRepository.findById(entity.getId()).orElseThrow();
            existing.setSlotId(entity.getSlotId());
            existing.setStatus(entity.getStatus());
            existing.setRespondedAt(entity.getRespondedAt());
            return toResponseDomain(responseJpaRepository.save(existing));
        }
        return toResponseDomain(responseJpaRepository.save(entity));
    }

    @Override
    public Optional<DonorResponse> findResponseById(Long id) {
        return responseJpaRepository.findById(id).map(this::toResponseDomain);
    }

    @Override
    public List<DonorResponse> findResponsesByEmergencyId(Long emergencyId) {
        return responseJpaRepository.findByEmergencyIdOrderByCreatedAtAsc(emergencyId)
                .stream().map(this::toResponseDomain).toList();
    }

    @Override
    public List<DonorResponse> findResponsesByDonorId(Long donorId) {
        return responseJpaRepository.findByDonorIdOrderByCreatedAtDesc(donorId)
                .stream().map(this::toResponseDomain).toList();
    }

    @Override
    public boolean existsByEmergencyIdAndDonorId(Long emergencyId, Long donorId) {
        return responseJpaRepository.existsByEmergencyIdAndDonorId(emergencyId, donorId);
    }

    private EmergencyRequestEntity toEntity(EmergencyRequest domain) {
        var entity = new EmergencyRequestEntity();
        entity.setId(domain.getId());
        entity.setPatientName(domain.getPatientName());
        entity.setBloodType(domain.getBloodType());
        entity.setUnitsNeeded(domain.getUnitsNeeded());
        entity.setUrgency(domain.getUrgency());
        entity.setHospital(domain.getHospital());
        entity.setHospitalAddress(domain.getHospitalAddress());
        entity.setLatitude(domain.getLatitude());
        entity.setLongitude(domain.getLongitude());
        entity.setContactPhone(domain.getContactPhone());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        return entity;
    }

    private EmergencyRequest toDomain(EmergencyRequestEntity entity) {
        var domain = new EmergencyRequest();
        domain.setId(entity.getId());
        domain.setPatientName(entity.getPatientName());
        domain.setBloodType(entity.getBloodType());
        domain.setUnitsNeeded(entity.getUnitsNeeded());
        domain.setUrgency(entity.getUrgency());
        domain.setHospital(entity.getHospital());
        domain.setHospitalAddress(entity.getHospitalAddress());
        domain.setLatitude(entity.getLatitude());
        domain.setLongitude(entity.getLongitude());
        domain.setContactPhone(entity.getContactPhone());
        domain.setStatus(entity.getStatus());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        domain.setExpiresAt(entity.getExpiresAt());
        return domain;
    }

    private DonorResponseEntity toResponseEntity(DonorResponse domain) {
        var entity = new DonorResponseEntity();
        entity.setId(domain.getId());
        entity.setEmergencyId(domain.getEmergencyId());
        entity.setDonorId(domain.getDonorId());
        entity.setSlotId(domain.getSlotId());
        entity.setStatus(domain.getStatus());
        entity.setRespondedAt(domain.getRespondedAt());
        return entity;
    }

    private DonorResponse toResponseDomain(DonorResponseEntity entity) {
        var domain = new DonorResponse();
        domain.setId(entity.getId());
        domain.setEmergencyId(entity.getEmergencyId());
        domain.setDonorId(entity.getDonorId());
        domain.setSlotId(entity.getSlotId());
        domain.setStatus(entity.getStatus());
        domain.setRespondedAt(entity.getRespondedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
