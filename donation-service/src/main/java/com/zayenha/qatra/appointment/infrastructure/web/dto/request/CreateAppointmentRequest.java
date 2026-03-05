package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateAppointmentRequest(
    @NotNull Long donorId,
    @NotNull Long slotId
) {}
