package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

import java.time.Instant;

public record AuditLogResponse(
    Long id,
    String eventType,
    Long actorId,
    String actorEmail,
    String targetType,
    Long targetId,
    String details,
    String sourceModule,
    Instant timestamp
) {}
