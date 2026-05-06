package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra.analytics.application.proxy.AnalyticsUserProxy;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AuditLogMapper {

    @Autowired
    protected AnalyticsUserProxy userProxy;

    @Mapping(target = "user", expression = "java(domain.getUserId() != null ? userProxy.getUserReference(domain.getUserId()) : null)")
    public abstract AuditLogEntity toEntity(AuditLog domain);

    @Mapping(target = "userId", source = "user.id")
    public abstract AuditLog toDomain(AuditLogEntity entity);
}
