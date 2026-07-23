package com.zayenha.qatra.user.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
    @NotNull UserStatus status
) {}
