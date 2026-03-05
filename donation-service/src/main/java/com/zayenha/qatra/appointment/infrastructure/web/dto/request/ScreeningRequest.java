package com.zayenha.qatra.appointment.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record ScreeningRequest(
    @NotNull Double weight,
    String bloodPressure,
    @NotNull Double hemoglobin,
    @NotNull Double temperature,
    @NotNull Boolean eligible,
    String notes
) {}
