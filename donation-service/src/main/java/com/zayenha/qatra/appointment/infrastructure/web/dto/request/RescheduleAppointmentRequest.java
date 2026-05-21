package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record RescheduleAppointmentRequest(
    @NotNull Long slotId
) {}
