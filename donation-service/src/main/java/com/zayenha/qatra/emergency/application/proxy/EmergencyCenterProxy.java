package com.zayenha.qatra.emergency.application.proxy;

import com.zayenha.qatra.center.application.api.CenterApi;
import com.zayenha.qatra.center.application.api.dto.CenterDTO;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmergencyCenterProxy {

    private final CenterApi centerApi;

    public CenterEntity getCenterReference(Long id) {
        return centerApi.getCenterReference(id);
    }

    public SlotEntity getSlotReference(Long id) {
        return centerApi.getSlotReference(id);
    }

    public Optional<CenterDTO> findCenterById(Long id) {
        return centerApi.findCenterById(id);
    }
}
