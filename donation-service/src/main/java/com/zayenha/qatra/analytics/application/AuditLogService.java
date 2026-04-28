package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepositoryPort repository;

    @Async
    @Transactional
    public void record(Long userId, String action, String entityType, Long entityId,
                       Map<String, Object> oldValue, Map<String, Object> newValue, String ipAddress) {
        var log = new AuditLog(userId, action, entityType, entityId, oldValue, newValue, ipAddress);
        repository.save(log);
    }

    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    public List<AuditLog> findByAction(String action) {
        return repository.findByAction(action);
    }

    public List<AuditLog> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public long countByAction(String action) {
        return repository.countByAction(action);
    }
}
