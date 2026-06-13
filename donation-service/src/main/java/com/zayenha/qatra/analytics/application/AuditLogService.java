package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.in.AuditLogQueryUseCases;
import com.zayenha.qatra.analytics.domain.model.CenterMetrics;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.domain.port.out.CenterMetricsRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService implements AuditLogQueryUseCases {

    private final AuditLogRepositoryPort repository;
    private final CenterMetricsRepositoryPort centerMetricsRepository;

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

    public PageResult<AuditLog> findFiltered(SearchCriteria criteria, String action, Instant fromDate, Instant toDate, Long centerId) {
        return repository.findFiltered(criteria, action, fromDate, toDate, centerId);
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

    public long countByActionBetween(String action, Instant from, Instant to) {
        return repository.countByActionBetween(action, from, to);
    }

    public CenterMetrics getCenterMetrics(Long centerId) {
        return centerMetricsRepository.getMetrics(centerId);
    }
}
