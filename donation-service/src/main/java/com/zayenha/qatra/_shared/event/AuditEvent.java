package com.zayenha.qatra._shared.event;

public record AuditEvent(
        Long userId,
        String action,
        String entityType,
        Long entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent
) {}
