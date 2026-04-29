package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.analytics.domain.port.in.AuditLogQueryUseCases;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.MetricsResponse;
import com.zayenha.qatra.analytics.infrastructure.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AnalyticsController {

    private final AuditLogQueryUseCases auditLogQueryUseCases;
    private final AnalyticsMapper mapper;

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(null, "timestamp", "desc", page, size);
        var result = auditLogQueryUseCases.findAll(criteria);
        return ResponseEntity.ok(ApiResponse.success(
            result.content().stream().map(mapper::toResponse).toList(),
            PageHelper.fromDomain(result)
        ));
    }

    @GetMapping("/audit-logs/by-action/{action}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByAction(@PathVariable String action) {
        var logs = auditLogQueryUseCases.findByAction(action);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/audit-logs/by-user/{userId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByUser(@PathVariable Long userId) {
        var logs = auditLogQueryUseCases.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<MetricsResponse>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(List.of(
            new MetricsResponse("APPOINTMENTS_CREATED", auditLogQueryUseCases.countByAction("APPOINTMENT_CREATED")),
            new MetricsResponse("APPOINTMENTS_COMPLETED", auditLogQueryUseCases.countByAction("APPOINTMENT_COMPLETED")),
            new MetricsResponse("EMERGENCIES_CREATED", auditLogQueryUseCases.countByAction("EMERGENCY_CREATED")),
            new MetricsResponse("EMERGENCIES_FULFILLED", auditLogQueryUseCases.countByAction("EMERGENCY_FULFILLED")),
            new MetricsResponse("DONOR_RESPONSES", auditLogQueryUseCases.countByAction("DONOR_RESPONSE"))
        )));
    }
}
