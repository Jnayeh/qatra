package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.domain.port.out.SlotRepositoryPort;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.SlotJpaRepository;
import com.zayenha.qatra._shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SlotRepositoryAdapter implements SlotRepositoryPort {

    private final SlotJpaRepository slotJpaRepository;
    private final CenterJpaRepository centerJpaRepository;
    private final CenterMapper mapper;

    public Slot save(Slot slot, Long centerId) {
        slot.setCenterId(centerId);
        var entity = mapper.toSlotEntity(slot);
        var saved = slotJpaRepository.save(entity);
        return mapper.toSlotDomain(saved);
    }

    public List<Slot> findByCenterIdAndDate(Long centerId, LocalDate date) {
        return slotJpaRepository.findByCenterIdAndDateOrderByStartTime(centerId, date)
                .stream().map(mapper::toSlotDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateWithJoins(Long centerId, LocalDate date) {
        return slotJpaRepository.findByCenterIdAndDateWithCenter(centerId, date)
                .stream().map(mapper::toSlotDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateRange(Long centerId, LocalDate from, LocalDate to) {
        return slotJpaRepository.findByCenterIdAndDateRange(centerId, from, to)
                .stream().map(mapper::toSlotDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateRangeWithJoins(Long centerId, LocalDate from, LocalDate to) {
        return slotJpaRepository.findByCenterIdAndDateRangeWithCenter(centerId, from, to)
                .stream().map(mapper::toSlotDomain).toList();
    }

    public Optional<Slot> findById(Long slotId) {
        return slotJpaRepository.findById(slotId).map(mapper::toSlotDomain);
    }

    public Optional<Slot> findByIdWithJoins(Long slotId) {
        return slotJpaRepository.findByIdWithCenter(slotId).map(mapper::toSlotDomain);
    }

    public List<Slot> findOverlapping(Long centerId, LocalDate date, String startTime, String endTime) {
        var start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        var end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        return slotJpaRepository.findOverlapping(centerId, date, start, end)
                .stream().map(mapper::toSlotDomain).toList();
    }

    public List<Slot> findAllByDateRange(LocalDate from, LocalDate to) {
        return slotJpaRepository.findAllByDateRange(from, to).stream().map(mapper::toSlotDomain).toList();
    }

    public Slot update(Slot slot) {
        var entity = slotJpaRepository.findById(slot.getId()).orElseThrow();
        entity.setBookedCount(slot.getBookedCount());
        entity.setRegularBookedCount(slot.getRegularBookedCount());
        entity.setBlocked(slot.isBlocked());
        var saved = slotJpaRepository.save(entity);
        return mapper.toSlotDomain(saved);
    }
}
