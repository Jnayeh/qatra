package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class AuditLogMapper {

    @Autowired
    protected UserJpaRepository userJpaRepository;

    @Mapping(target = "user", expression = "java(domain.getUserId() != null ? userJpaRepository.getReferenceById(domain.getUserId()) : null)")
    public abstract AuditLogEntity toEntity(AuditLog domain);

    @Mapping(target = "userId", source = "user.id")
    public abstract AuditLog toDomain(AuditLogEntity entity);
}
