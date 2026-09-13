package com.zayenha.qatra._shared.event;

import com.zayenha.qatra._shared.domain.enums.LogAction;

import java.util.Map;

public record AuditEvent(
        Long userId,
        LogAction action,
        String entityType,
        Long entityId,
        Map<String, Object> oldValue,
        Map<String, Object> newValue,
        String ipAddress,
        String userAgent
) {}
