package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

public interface CancelAppointmentUseCase {
    void cancel(CancelAppointmentCommand command);

    record CancelAppointmentCommand(
            AppointmentId appointmentId,
            String reason
    ) {}
}
