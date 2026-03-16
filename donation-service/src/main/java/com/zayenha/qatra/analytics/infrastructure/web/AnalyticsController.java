package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.analytics.application.AuditLogService;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.MetricsResponse;
import com.zayenha.qatra.analytics.infrastructure.web.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AuditLogService auditLogService;

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(null, "timestamp", "desc", page, size);
        var result = auditLogService.findAll(criteria);
        return ResponseEntity.ok(ApiResponse.success(
            result.content().stream().map(AnalyticsMapper::toResponse).toList(),
            PageHelper.fromDomain(result)
        ));
    }

    @GetMapping("/audit-logs/by-event/{eventType}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByEventType(@PathVariable String eventType) {
        var logs = auditLogService.findByEventType(eventType);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(AnalyticsMapper::toResponse).toList()));
    }

    @GetMapping("/audit-logs/by-actor/{actorId}")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByActor(@PathVariable Long actorId) {
        var logs = auditLogService.findByActorId(actorId);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(AnalyticsMapper::toResponse).toList()));
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<List<MetricsResponse>>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(List.of(
            new MetricsResponse("APPOINTMENTS_CREATED", auditLogService.countByEventType("APPOINTMENT_CREATED")),
            new MetricsResponse("APPOINTMENTS_COMPLETED", auditLogService.countByEventType("APPOINTMENT_COMPLETED")),
            new MetricsResponse("EMERGENCIES_CREATED", auditLogService.countByEventType("EMERGENCY_CREATED")),
            new MetricsResponse("EMERGENCIES_FULFILLED", auditLogService.countByEventType("EMERGENCY_FULFILLED")),
            new MetricsResponse("DONOR_RESPONSES", auditLogService.countByEventType("DONOR_RESPONSE"))
        )));
    }
}
