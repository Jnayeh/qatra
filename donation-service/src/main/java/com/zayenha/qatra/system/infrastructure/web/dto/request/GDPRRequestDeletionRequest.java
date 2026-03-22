package com.zayenha.qatra.system.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record GDPRRequestDeletionRequest(
    @NotNull Long userId,
    String reason
) {}
