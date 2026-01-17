package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface ViewAppointmentsUseCase {
    List<Appointment> getDonorHistory(Long donorId);
    List<Appointment> getDailySchedule(Long centerId, LocalDate date);
    AllAppointmentsResult getAllAppointments(AppointmentsQuery query);

    record AppointmentsQuery(Long centerId, LocalDate from, LocalDate to, int page, int size, String sortBy, String sortDir) {}
    record AllAppointmentsResult(List<Appointment> appointments, long totalElements) {}
}
