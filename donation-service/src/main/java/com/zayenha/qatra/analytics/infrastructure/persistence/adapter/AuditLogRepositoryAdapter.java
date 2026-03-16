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
    public List<AuditLog> findByEventType(String eventType) {
        return jpaRepository.findByEventTypeOrderByTimestampDesc(eventType).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByActorId(Long actorId) {
        return jpaRepository.findByActorIdOrderByTimestampDesc(actorId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByTimestampBetween(Instant from, Instant to) {
        return jpaRepository.findByTimestampBetweenOrderByTimestampDesc(from, to).stream().map(this::toDomain).toList();
    }

    @Override
    public long countByEventType(String eventType) {
        return jpaRepository.countByEventType(eventType);
    }

    private AuditLogEntity toEntity(AuditLog domain) {
        var entity = new AuditLogEntity();
        entity.setId(domain.getId());
        entity.setEventType(domain.getEventType());
        entity.setActorId(domain.getActorId());
        entity.setActorEmail(domain.getActorEmail());
        entity.setTargetType(domain.getTargetType());
        entity.setTargetId(domain.getTargetId());
        entity.setDetails(domain.getDetails());
        entity.setSourceModule(domain.getSourceModule());
        entity.setTimestamp(domain.getTimestamp());
        return entity;
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        var domain = new AuditLog();
        domain.setId(entity.getId());
        domain.setEventType(entity.getEventType());
        domain.setActorId(entity.getActorId());
        domain.setActorEmail(entity.getActorEmail());
        domain.setTargetType(entity.getTargetType());
        domain.setTargetId(entity.getTargetId());
        domain.setDetails(entity.getDetails());
        domain.setSourceModule(entity.getSourceModule());
        domain.setTimestamp(entity.getTimestamp());
        return domain;
    }
}
