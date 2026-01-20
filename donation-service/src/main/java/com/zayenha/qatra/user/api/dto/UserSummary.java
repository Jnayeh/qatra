package com.zayenha.qatra.user.api.dto;

import com.zayenha.qatra.user.domain.model.UserStatus;

public record UserSummary(
    Long id,
    String email,
    String phone,
    String displayName,
    UserStatus status
) {}
