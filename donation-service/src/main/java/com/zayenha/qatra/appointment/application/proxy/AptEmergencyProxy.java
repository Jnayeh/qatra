package com.zayenha.qatra.appointment.application.proxy;

import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AptEmergencyProxy {

    private final EntityApi<EmergencyRequestEntity> emergencyApi;

    public EmergencyRequestEntity getEmergencyReference(Long id) {
        return emergencyApi.getReference(id);
    }
}
