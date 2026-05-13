package com.zayenha.qatra.user.infrastructure.web.dto.response;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.UserStatus;
import java.time.Instant;
import java.util.List;

public record UserDetailResponse(
    Long id,
    String email,
    String phone,
    String displayName,
    UserStatus status,
    boolean emailVerified,
    List<Role> roles,
    Instant createdAt,
    Instant deletionRequestedAt,
    Instant deletedAt,
    Instant lastActiveAt
) {}
