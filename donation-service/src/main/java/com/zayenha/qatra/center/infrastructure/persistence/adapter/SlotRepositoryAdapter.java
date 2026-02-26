package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
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
public class SlotRepositoryAdapter {

    private final SlotJpaRepository slotJpaRepository;
    private final CenterJpaRepository centerJpaRepository;

    public Slot save(Slot slot, Long centerId) {
        var entity = toEntity(slot, centerId);
        var saved = slotJpaRepository.save(entity);
        return toDomain(saved);
    }

    public List<Slot> findByCenterIdAndDate(Long centerId, LocalDate date) {
        return slotJpaRepository.findByCenterIdAndDateOrderByStartTime(centerId, date)
                .stream().map(this::toDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateWithJoins(Long centerId, LocalDate date) {
        return slotJpaRepository.findByCenterIdAndDateWithCenter(centerId, date)
                .stream().map(this::toDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateRange(Long centerId, LocalDate from, LocalDate to) {
        return slotJpaRepository.findByCenterIdAndDateRange(centerId, from, to)
                .stream().map(this::toDomain).toList();
    }

    public List<Slot> findByCenterIdAndDateRangeWithJoins(Long centerId, LocalDate from, LocalDate to) {
        return slotJpaRepository.findByCenterIdAndDateRangeWithCenter(centerId, from, to)
                .stream().map(this::toDomain).toList();
    }

    public Optional<Slot> findById(Long slotId) {
        return slotJpaRepository.findById(slotId).map(this::toDomain);
    }

    public Optional<Slot> findByIdWithJoins(Long slotId) {
        return slotJpaRepository.findByIdWithCenter(slotId).map(this::toDomain);
    }

    public List<Slot> findOverlapping(Long centerId, LocalDate date, String startTime, String endTime) {
        var start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        var end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        return slotJpaRepository.findOverlapping(centerId, date, start, end)
                .stream().map(this::toDomain).toList();
    }

    public List<Slot> findAllByDateRange(LocalDate from, LocalDate to) {
        return slotJpaRepository.findAllByDateRange(from, to).stream().map(this::toDomain).toList();
    }

    public Slot update(Slot slot) {
        var entity = slotJpaRepository.findById(slot.getId()).orElseThrow();
        entity.setBookedCount(slot.getBookedCount());
        entity.setRegularBookedCount(slot.getRegularBookedCount());
        entity.setBlocked(slot.isBlocked());
        var saved = slotJpaRepository.save(entity);
        return toDomain(saved);
    }

    private SlotEntity toEntity(Slot slot, Long centerId) {
        var entity = new SlotEntity();
        entity.setId(slot.getId());
        entity.setCenter(centerJpaRepository.getReferenceById(centerId));
        entity.setDate(slot.getDate());
        entity.setStartTime(slot.getStartTime());
        entity.setEndTime(slot.getEndTime());
        entity.setMaxBookings(slot.getMaxBookings());
        entity.setMaxRegularBookings(slot.getMaxRegularBookings());
        entity.setBookedCount(slot.getBookedCount());
        entity.setRegularBookedCount(slot.getRegularBookedCount());
        entity.setBlocked(slot.isBlocked());
        return entity;
    }

    private Slot toDomain(SlotEntity entity) {
        var slot = new Slot();
        slot.setId(entity.getId());
        slot.setCenterId(entity.getCenter().getId());
        slot.setDate(entity.getDate());
        slot.setStartTime(entity.getStartTime());
        slot.setEndTime(entity.getEndTime());
        slot.setMaxBookings(entity.getMaxBookings());
        slot.setMaxRegularBookings(entity.getMaxRegularBookings());
        slot.setBookedCount(entity.getBookedCount());
        slot.setRegularBookedCount(entity.getRegularBookedCount());
        slot.setBlocked(entity.isBlocked());
        slot.setCreatedAt(entity.getCreatedAt());
        return slot;
    }
}
