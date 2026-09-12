package com.zayenha.qatra.analytics.domain.port.out;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.enums.LogAction;
import com.zayenha.qatra.analytics.domain.model.AuditLog;

import java.time.Instant;
import java.util.List;

public interface AuditLogRepositoryPort {
    AuditLog save(AuditLog log);
    PageResult<AuditLog> findAll(SearchCriteria criteria);
    PageResult<AuditLog> findFiltered(SearchCriteria criteria, LogAction action, Instant fromDate, Instant toDate, Long centerId);
    List<AuditLog> findByAction(LogAction action);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByTimestampBetween(Instant from, Instant to);
    long countByAction(LogAction action);
    long countByActionBetween(LogAction action, Instant from, Instant to);
}
