package com.zayenha.qatra.user.infrastructure.web.dto.response;

public record RestrictedUserResponse(
    Long id,
    String email,
    String displayName,
    String status,
    Long donorId,
    Boolean permanentlyRestricted,
    String restrictionReason
) {}
