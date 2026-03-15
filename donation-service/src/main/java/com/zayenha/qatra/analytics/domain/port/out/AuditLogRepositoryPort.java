package com.zayenha.qatra.analytics.domain.port.out;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog log);
    PageResult<AuditLog> findAll(SearchCriteria criteria);
    List<AuditLog> findByEventType(String eventType);
    List<AuditLog> findByActorId(Long actorId);
    List<AuditLog> findByTimestampBetween(Instant from, Instant to);
    long countByEventType(String eventType);
}
