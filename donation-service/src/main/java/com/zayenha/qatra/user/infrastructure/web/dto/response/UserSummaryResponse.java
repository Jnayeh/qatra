package com.zayenha.qatra.user.infrastructure.web.dto.response;

import com.zayenha.qatra.user.domain.model.UserStatus;

public record UserSummaryResponse(
    Long id,
    String email,
    String phone,
    String displayName,
    UserStatus status
) {}
