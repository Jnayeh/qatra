package com.zayenha.qatra.center.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.domain.enums.CenterStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCenterStatusRequest(
    @NotNull CenterStatus status
) {}
