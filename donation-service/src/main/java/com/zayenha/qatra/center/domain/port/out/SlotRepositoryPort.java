package com.zayenha.qatra.center.domain.port.out;

import com.zayenha.qatra.center.domain.model.Slot;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotRepositoryPort {
    Slot save(Slot slot, Long centerId);
    List<Slot> findByCenterIdAndDate(Long centerId, LocalDate date);
    List<Slot> findByCenterIdAndDateWithJoins(Long centerId, LocalDate date);
    List<Slot> findByCenterIdAndDateRange(Long centerId, LocalDate from, LocalDate to);
    List<Slot> findByCenterIdAndDateRangeWithJoins(Long centerId, LocalDate from, LocalDate to);
    Optional<Slot> findById(Long slotId);
    Optional<Slot> findByIdWithJoins(Long slotId);
    List<Slot> findOverlapping(Long centerId, LocalDate date, String startTime, String endTime);
    List<Slot> findAllByDateRange(LocalDate from, LocalDate to);
    Slot update(Slot slot);
}
