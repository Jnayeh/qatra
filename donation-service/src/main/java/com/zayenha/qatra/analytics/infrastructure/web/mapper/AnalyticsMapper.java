package com.zayenha.qatra.analytics.infrastructure.web.mapper;

import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;

public final class AnalyticsMapper {

    private AnalyticsMapper() {}

    public static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
            log.getId(), log.getEventType(), log.getActorId(), log.getActorEmail(),
            log.getTargetType(), log.getTargetId(), log.getDetails(), log.getSourceModule(), log.getTimestamp()
        );
    }
}
