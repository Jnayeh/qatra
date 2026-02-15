package com.zayenha.qatra.center.infrastructure.web.dto.request;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateCenterStatusRequest(
    @NotNull CenterStatus status
) {}
