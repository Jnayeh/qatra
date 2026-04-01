package com.zayenha.qatra.user.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String email,
    @NotBlank String password
) {}
