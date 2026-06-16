package com.zayenha.qatra.center.application.api;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.center.application.api.dto.CenterDTO;
import com.zayenha.qatra.center.application.api.dto.SlotDTO;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.port.out.SlotRepositoryPort;
import com.zayenha.qatra.center.infrastructure.mapper.SlotMapper;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.SlotJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CenterApi implements EntityApi<CenterEntity> {

    private final CenterJpaRepository centerJpaRepository;
    private final SlotJpaRepository slotJpaRepository;
    private final CenterRepositoryPort centerRepositoryPort;
    private final SlotRepositoryPort slotRepositoryPort;
    private final SlotMapper slotMapper;

    public CenterEntity getCenterReference(Long id) {
        return centerJpaRepository.getReferenceById(id);
    }

    @Override
    public CenterEntity getReference(Long id) {
        return getCenterReference(id);
    }

    public SlotEntity getSlotReference(Long id) {
        return slotJpaRepository.getReferenceById(id);
    }

    public Optional<CenterDTO> findCenterById(Long id) {
        return centerRepositoryPort.findById(id).map(c ->
            new CenterDTO(c.getId(), c.getLatitude(), c.getLongitude()));
    }
    public boolean existsById(Long id) {
        return centerRepositoryPort.existsById(id);
    }

    public Optional<CenterDTO> findCenterByIdWithJoins(Long id) {
        return centerRepositoryPort.findById(id, true).map(c ->
            new CenterDTO(c.getId(), c.getLatitude(), c.getLongitude()));
    }

    public SlotDTO findSlotById(Long slotId) {
        return slotRepositoryPort.findById(slotId).map(slotMapper::toSlotDto)
                .orElseThrow(() -> new NotFoundException("Slot not found: " + slotId, "SLOT_NOT_FOUND"));
    }

    public SlotDTO updateSlot(SlotDTO dto) {
        var slot = slotRepositoryPort.findById(dto.getId())
            .orElseThrow(() -> new NotFoundException("Slot not found: " + dto.getId(), "SLOT_NOT_FOUND"));
        slot.setBookedCount(dto.getBookedCount());
        slot.setRegularBookedCount(dto.getRegularBookedCount());
        slot.validateCounts();
        var saved = slotRepositoryPort.update(slot);
        return slotMapper.toSlotDto(saved);
    }
}
