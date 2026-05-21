package com.zayenha.qatra.appointment.application.proxy;

import com.zayenha.qatra.center.application.api.CenterApi;
import com.zayenha.qatra.center.application.api.dto.SlotDTO;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AptCenterProxy {

    private final CenterApi centerApi;

    public CenterEntity getCenterReference(Long id) {
        return centerApi.getCenterReference(id);
    }

    public SlotEntity getSlotReference(Long id) {
        return centerApi.getSlotReference(id);
    }

    public SlotDTO findSlotById(Long slotId) {
        return centerApi.findSlotById(slotId);
    }

    public SlotDTO updateSlot(SlotDTO dto) {
        return centerApi.updateSlot(dto);
    }
}
