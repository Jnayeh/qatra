package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.enums.LogAction;
import com.zayenha.qatra._shared.domain.enums.Role;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.in.AuditLogQueryUseCases;
import com.zayenha.qatra.analytics.domain.model.CenterMetrics;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.domain.port.out.CenterMetricsRepositoryPort;
import com.zayenha.qatra.analytics.application.proxy.AnalyticsCenterProxy;
import com.zayenha.qatra.analytics.application.proxy.AnalyticsEmergencyProxy;
import com.zayenha.qatra.analytics.application.proxy.AnalyticsUserProxy;
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
    private final AnalyticsUserProxy userProxy;
    private final AnalyticsCenterProxy centerProxy;
    private final AnalyticsEmergencyProxy emergencyProxy;

    @Async
    @Transactional
    public void record(Long userId, LogAction action, String entityType, Long entityId,
                       Map<String, Object> oldValue, Map<String, Object> newValue, String ipAddress) {
        var id = userId == 0 ? null : userId;
        var log = new AuditLog(id, action.name(), entityType, entityId, oldValue, newValue, ipAddress);
        repository.save(log);
    }

    public PageResult<AuditLog> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    public PageResult<AuditLog> findFiltered(SearchCriteria criteria, LogAction action, Instant fromDate, Instant toDate, Long centerId) {
        return repository.findFiltered(criteria, action, fromDate, toDate, centerId);
    }

    public List<AuditLog> findByAction(LogAction action) {
        return repository.findByAction(action);
    }

    public List<AuditLog> findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }

    public long countByAction(LogAction action) {
        return repository.countByAction(action);
    }

    public long countByActionBetween(LogAction action, Instant from, Instant to) {
        return repository.countByActionBetween(action, from, to);
    }

    public CenterMetrics getCenterMetrics(Long centerId) {
        return centerMetricsRepository.getMetrics(centerId);
    }

    @Override
    public long countDonors() {
        return userProxy.countDonors();
    }

    @Override
    public long countActiveCenters() {
        return centerProxy.countActiveCenters();
    }

    @Override
    public long countDonorResponses() {
        return emergencyProxy.countDonorResponses();
    }

    @Override
    public long countDonorResponsesAccepted() {
        return emergencyProxy.countDonorResponsesAccepted();
    }
}
