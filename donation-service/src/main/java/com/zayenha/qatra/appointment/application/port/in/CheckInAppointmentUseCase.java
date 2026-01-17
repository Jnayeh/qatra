package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

public interface CheckInAppointmentUseCase {
    void checkIn(CheckInAppointmentCommand command);

    record CheckInAppointmentCommand(
            AppointmentId appointmentId
    ) {}
}
