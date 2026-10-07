package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record CompleteAppointmentRequest(
    @NotNull String outcome,
    Integer mlCollected,
    String notes
) {}
