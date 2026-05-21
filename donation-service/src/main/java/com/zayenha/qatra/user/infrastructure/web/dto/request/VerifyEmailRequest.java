package com.zayenha.qatra.user.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
    @NotBlank String token
) {}
