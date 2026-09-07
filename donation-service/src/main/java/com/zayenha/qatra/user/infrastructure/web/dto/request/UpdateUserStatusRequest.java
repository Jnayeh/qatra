package com.zayenha.qatra.user.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull UserStatus status
) {}
