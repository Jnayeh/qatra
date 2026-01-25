package com.zayenha.qatra.user.infrastructure.web.dto.request;

public record UserSearchRequest(
    String email,
    String phone,
    String displayName
) {}
