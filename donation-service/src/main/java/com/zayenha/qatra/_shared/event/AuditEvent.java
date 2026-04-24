package com.zayenha.qatra._shared.event;

import java.util.Map;

public record AuditEvent(
        Long userId,
        String action,
        String entityType,
        Long entityId,
        Map<String, Object> oldValue,
        Map<String, Object> newValue,
        String ipAddress,
        String userAgent
) {}
