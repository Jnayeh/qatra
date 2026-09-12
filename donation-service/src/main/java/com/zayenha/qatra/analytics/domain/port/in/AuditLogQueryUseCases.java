package com.zayenha.qatra.analytics.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.enums.LogAction;
import com.zayenha.qatra._shared.domain.enums.Role;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.model.CenterMetrics;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;

import java.time.Instant;
import java.util.List;

public interface AuditLogQueryUseCases {

    PageResult<AuditLog> findAll(SearchCriteria criteria);

    PageResult<AuditLog> findFiltered(SearchCriteria criteria, LogAction action, Instant fromDate, Instant toDate, Long centerId);

    List<AuditLog> findByAction(LogAction action);

    List<AuditLog> findByUserId(Long userId);

    long countByAction(LogAction action);

    long countByActionBetween(LogAction action, Instant from, Instant to);

    CenterMetrics getCenterMetrics(Long centerId);

    long countDonors();

    long countActiveCenters();

    long countDonorResponses();

    long countDonorResponsesAccepted();
}
