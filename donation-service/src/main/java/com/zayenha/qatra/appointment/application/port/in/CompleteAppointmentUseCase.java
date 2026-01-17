package com.zayenha.qatra.appointment.application.port.in;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

public interface CompleteAppointmentUseCase {
    void complete(CompleteAppointmentCommand command);

    record CompleteAppointmentCommand(
            AppointmentId appointmentId,
            Integer mlCollected,
            Long completedByStaffId
    ) {}
}
