package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.exception.BadRequestException;
import com.zayenha.qatra.appointment.domain.exception.AppointmentErrorCode;
import com.zayenha.qatra._shared.domain.AppointmentType;
import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
    @NotNull AppointmentType type,
    @NotNull Long donorId,
    @NotNull Long slotId,
    Long emergencyId
) {
    public static void validate(CreateAppointmentRequest request) {
        if (request.emergencyId() == null && AppointmentType.EMERGENCY.equals(request.type())) {
            throw new BadRequestException("Emergency ID must not be null", AppointmentErrorCode.EMERGENCY_APPOINTMENT_INCOMPLETE.name());
        }
    }
}
