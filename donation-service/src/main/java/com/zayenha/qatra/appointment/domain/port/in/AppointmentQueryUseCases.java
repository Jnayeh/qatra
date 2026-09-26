package com.zayenha.qatra.appointment.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentQueryUseCases {
    Appointment findById(Long id);
    PageResult<Appointment> findByDonorId(Long donorId, AppointmentStatus status, int page, int size);
    List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date);
    PageResult<Appointment> findByCenterIdAndDateRange(
        Long centerId, LocalDate fromDate, LocalDate toDate, int page, int size);
    PageResult<Appointment> findAll(SearchCriteria criteria);
    HealthScreening findScreeningByAppointmentId(Long appointmentId);
    List<Appointment> findScheduledAppointmentsByDate(LocalDate targetDate);
}
