package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CompleteAppointmentRequest(
    @NotNull String outcome,
    @NotNull @Min(1) Integer mlCollected,
    String notes
) {}
