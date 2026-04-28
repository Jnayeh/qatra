package com.zayenha.qatra.analytics.infrastructure.mapper;

import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsMapper {

    AuditLogResponse toResponse(AuditLog log);
}
