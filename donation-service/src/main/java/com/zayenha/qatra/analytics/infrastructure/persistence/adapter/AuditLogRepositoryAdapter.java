package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    public AuditLog save(AuditLog log) {
        return toDomain(jpaRepository.save(toEntity(log)));
    }

    @Override
    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        var pageable = PageRequest.of(criteria.page(), criteria.size());
        var page = jpaRepository.findAllByOrderByTimestampDesc(pageable);
        return new PageResult<>(
            page.getContent().stream().map(this::toDomain).toList(),
            page.getNumber(), page.getSize(),
            page.getTotalElements(), page.getTotalPages()
        );
    }

    @Override
    public List<AuditLog> findByAction(String action) {
        return jpaRepository.findByActionOrderByTimestampDesc(action).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByUserId(Long userId) {
        return jpaRepository.findByUserIdOrderByTimestampDesc(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByTimestampBetween(Instant from, Instant to) {
        return jpaRepository.findByTimestampBetweenOrderByTimestampDesc(from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public long countByAction(String action) {
        return jpaRepository.countByAction(action);
    }

    private AuditLogEntity toEntity(AuditLog domain) {
        var entity = new AuditLogEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setAction(domain.getAction());
        entity.setEntityType(domain.getEntityType());
        entity.setEntityId(domain.getEntityId());
        entity.setOldValue(domain.getOldValue());
        entity.setNewValue(domain.getNewValue());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        entity.setTimestamp(domain.getTimestamp());
        return entity;
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        var domain = new AuditLog();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setAction(entity.getAction());
        domain.setEntityType(entity.getEntityType());
        domain.setEntityId(entity.getEntityId());
        domain.setOldValue(entity.getOldValue());
        domain.setNewValue(entity.getNewValue());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        domain.setTimestamp(entity.getTimestamp());
        return domain;
    }
}
