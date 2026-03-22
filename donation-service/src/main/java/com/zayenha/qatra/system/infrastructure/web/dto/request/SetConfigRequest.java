package com.zayenha.qatra.system.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SetConfigRequest(
    @NotBlank String key,
    @NotBlank String value,
    String description
) {}
