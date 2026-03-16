package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepositoryPort repository;

    @Async
    @Transactional
    public void record(String eventType, Long actorId, String targetType, Long targetId, String details, String sourceModule) {
        var log = new AuditLog(eventType, actorId, targetType, targetId, details, sourceModule);
        repository.save(log);
    }


    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    public List<AuditLog> findByEventType(String eventType) {
        return repository.findByEventType(eventType);
    }

    public List<AuditLog> findByActorId(Long actorId) {
        return repository.findByActorId(actorId);
    }

    public long countByEventType(String eventType) {
        return repository.countByEventType(eventType);
    }
}
