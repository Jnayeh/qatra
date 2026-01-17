package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

public interface RescheduleAppointmentUseCase {
    void reschedule(RescheduleAppointmentCommand command);

    record RescheduleAppointmentCommand(
            AppointmentId appointmentId,
            Long newSlotId
    ) {}
}
