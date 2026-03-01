package com.zayenha.qatra.center.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record AddStaffRequest(
    @NotNull Long userId
) {}
