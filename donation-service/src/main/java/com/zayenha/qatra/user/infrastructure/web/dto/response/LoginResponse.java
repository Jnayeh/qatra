package com.zayenha.qatra.user.infrastructure.web.dto.response;

import com.zayenha.qatra.user.domain.model.Role;

import java.util.List;

public record LoginResponse(
    String token,
    String tokenType,
    Long userId,
    String email,
    String displayName,
    List<Role> roles
) {
    public LoginResponse(String token, Long userId, String email, String displayName, List<Role> roles) {
        this(token, "Bearer", userId, email, displayName, roles);
    }
}
