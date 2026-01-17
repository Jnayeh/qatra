package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

public interface MarkNoShowUseCase {
    void markNoShow(MarkNoShowCommand command);

    record MarkNoShowCommand(
            AppointmentId appointmentId
    ) {}
}
