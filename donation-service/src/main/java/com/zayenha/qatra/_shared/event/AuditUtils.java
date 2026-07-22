package com.zayenha.qatra._shared.event;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditUtils {

    private AuditUtils() {}

    @NotNull
    public static Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return 0L;
        var principal = auth.getPrincipal();
        if ("anonymousUser".equals(principal)) return 0L;
        if (principal instanceof Long id) return id;
        return 0L;
    }
    public static Long currentUserId(Long userId) {
        if(currentUserId().equals(0L)) return userId;
        return currentUserId();
    }
}
