package com.zayenha.qatra.appointment.domain.port.out;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Optional<Appointment> findById(AppointmentId id);
    List<Appointment> findByDonorId(Long donorId);
    List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date);
    Appointment save(Appointment appointment);
    void delete(AppointmentId id);

    long countByStatus(AppointmentStatus status);
    long totalMlCollected();
    long totalMlCollectedBetween(LocalDateTime start, LocalDateTime end);
    long countCompletedBetween(LocalDateTime start, LocalDateTime end);
    long countByCenterIdAndStatus(Long centerId, AppointmentStatus status);
    List<Object[]> donationSummaryByCenter();
    List<Object[]> dailyDonationStats(LocalDateTime start, LocalDateTime end);

    List<Appointment> findAll(Long centerId, LocalDate from, LocalDate to, int page, int size, String sortBy, String sortDir);
    long countAll(Long centerId, LocalDate from, LocalDate to);
}
