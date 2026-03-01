package com.zayenha.qatra.center.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record ApproveCenterRequest(
    @NotNull boolean approved,
    String reason
) {}
