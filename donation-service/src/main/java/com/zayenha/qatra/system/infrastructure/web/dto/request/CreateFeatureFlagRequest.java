package com.zayenha.qatra.system.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateFeatureFlagRequest(
    @NotBlank String flagName,
    boolean enabled,
    String description
) {}
