package com.zayenha.qatra._shared.event;

import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditUtils {

    private AuditUtils() {}

    public static Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return 0L;
        var principal = auth.getPrincipal();
        if ("anonymousUser".equals(principal)) return 0L;
        if (principal instanceof Long id) return id;
        return 0L;
    }
}
