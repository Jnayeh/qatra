package com.zayenha.qatra.user.infrastructure.web.dto.response;

import com.zayenha.qatra.user.domain.model.Role;

import java.util.List;

public record LoginResponse(
    String token,
    String tokenType,
    String refreshToken,
    Long userId,
    String email,
    String displayName,
    List<Role> roles
) {
    public LoginResponse(String token, String refreshToken, Long userId, String email, String displayName, List<Role> roles) {
        this(token, "Bearer", refreshToken, userId, email, displayName, roles);
    }
}
