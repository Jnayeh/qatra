package com.zayenha.qatra.analytics.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;

import java.time.Instant;
import java.util.List;

public interface AuditLogQueryUseCases {

    PageResult<AuditLog> findAll(SearchCriteria criteria);

    PageResult<AuditLog> findFiltered(SearchCriteria criteria, String action, Instant fromDate, Instant toDate);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByUserId(Long userId);

    long countByAction(String action);

    long countByActionBetween(String action, Instant from, Instant to);
}
