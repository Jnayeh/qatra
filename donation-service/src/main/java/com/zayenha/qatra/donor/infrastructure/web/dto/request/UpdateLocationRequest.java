package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateLocationRequest(
    @NotNull Double latitude,
    @NotNull Double longitude,
    String city,
    String country
) {}
