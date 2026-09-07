package com.zayenha.qatra.user.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.domain.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
    @NotNull Role role
) {}
