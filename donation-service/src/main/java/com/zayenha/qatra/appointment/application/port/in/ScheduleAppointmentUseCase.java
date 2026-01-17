package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentType;

public interface ScheduleAppointmentUseCase {
    Appointment schedule(ScheduleAppointmentCommand command);

    record ScheduleAppointmentCommand(
            Long donorId,
            Long centerId,
            Long slotId,
            AppointmentType appointmentType,
            Long emergencyId
    ) {}
}
