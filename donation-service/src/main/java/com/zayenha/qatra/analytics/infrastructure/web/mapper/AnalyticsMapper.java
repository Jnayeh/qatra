package com.zayenha.qatra.analytics.infrastructure.web.mapper;

import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;

public final class AnalyticsMapper {

    private AnalyticsMapper() {}

    public static AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
            log.getId(), log.getUserId(), log.getAction(), log.getEntityType(), log.getEntityId(),
            log.getOldValue(), log.getNewValue(), log.getIpAddress(), log.getUserAgent(), log.getTimestamp()
        );
    }
}
