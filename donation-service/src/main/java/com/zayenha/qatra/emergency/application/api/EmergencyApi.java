package com.zayenha.qatra.emergency.application.api;

import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.emergency.domain.model.ResponseStatus;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.DonorResponseJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyApi implements EntityApi<EmergencyRequestEntity> {

    private final EmergencyJpaRepository emergencyJpaRepository;
    private final DonorResponseJpaRepository donorResponseJpaRepository;

    public EmergencyRequestEntity getEmergencyReference(Long id) {
        return emergencyJpaRepository.getReferenceById(id);
    }

    public long countDonorResponses() {
        return donorResponseJpaRepository.count();
    }

    public long countDonorResponsesAccepted() {
        return donorResponseJpaRepository.countByStatus(ResponseStatus.ACCEPTED);
    }

    @Override
    public EmergencyRequestEntity getReference(Long id) {
        return getEmergencyReference(id);
    }
}
