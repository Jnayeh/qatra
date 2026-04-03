package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    Long userId,
    String action,
    String entityType,
    Long entityId,
    String oldValue,
    String newValue,
    String ipAddress,
    String userAgent,
    Instant timestamp
) {}
