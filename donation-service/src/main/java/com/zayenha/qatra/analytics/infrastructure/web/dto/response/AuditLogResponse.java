package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.Map;

public record AuditLogResponse(
    Long id,
    Long userId,
    String action,
    String entityType,
    Long entityId,
    Map<String, Object> oldValue,
    Map<String, Object> newValue,
    String ipAddress,
    Instant timestamp
) {}
