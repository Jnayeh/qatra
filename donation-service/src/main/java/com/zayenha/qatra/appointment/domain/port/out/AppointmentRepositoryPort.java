package com.zayenha.qatra.appointment.domain.port.out;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepositoryPort {
    Appointment save(Appointment appointment);
    Optional<Appointment> findById(Long id);
    List<Appointment> findByDonorId(Long donorId);
    List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date);
    PageResult<Appointment> findAll(SearchCriteria criteria);
    boolean existsByDonorIdAndStatusIn(Long donorId, List<com.zayenha.qatra.appointment.domain.model.AppointmentStatus> statuses);
    HealthScreening saveScreening(HealthScreening screening);
    Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId);
    List<Appointment> findByEmergencyId(Long emergencyId);
}
