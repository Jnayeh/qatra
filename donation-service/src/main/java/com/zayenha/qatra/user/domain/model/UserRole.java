package com.zayenha.qatra.user.domain.model;

import com.zayenha.qatra._shared.domain.Role;
import lombok.Getter;

import java.time.Instant;

@Getter
public class UserRole {
    private Long id;
    private Long userId;
    private Role role;
    private Instant assignedAt;

    public UserRole(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
        this.assignedAt = Instant.now();
    }

    private UserRole() {}

    public static UserRole reconstruct(Long id, Long userId, Role role, Instant assignedAt) {
        var r = new UserRole();
        r.id = id;
        r.userId = userId;
        r.role = role;
        r.assignedAt = assignedAt;
        return r;
    }

}
