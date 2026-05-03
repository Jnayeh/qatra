package com.zayenha.qatra.emergency.application.api;

import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyApi implements EntityApi<EmergencyRequestEntity> {

    private final EmergencyJpaRepository emergencyJpaRepository;

    public EmergencyRequestEntity getEmergencyReference(Long id) {
        return emergencyJpaRepository.getReferenceById(id);
    }

    @Override
    public EmergencyRequestEntity getReference(Long id) {
        return getEmergencyReference(id);
    }
}
