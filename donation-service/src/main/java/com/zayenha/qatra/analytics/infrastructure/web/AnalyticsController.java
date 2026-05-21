package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.cache.CacheService;
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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AnalyticsController {

    private static final List<String> METRIC_ACTIONS = List.of(
        "APPOINTMENT_CREATED", "APPOINTMENT_COMPLETED",
        "EMERGENCY_CREATED", "EMERGENCY_FULFILLED",
        "DONOR_RESPONSE"
    );

    private final AuditLogQueryUseCases auditLogQueryUseCases;
    private final AnalyticsMapper mapper;
    private final CacheService cacheService;

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
        var cacheKey = "metrics:overview";
        var cached = cacheService.get(cacheKey, CacheableMetricsList.class);
        if (cached.isPresent()) return ResponseEntity.ok(ApiResponse.success(cached.get().metrics));

        var now = Instant.now();
        var today = LocalDate.now();
        var zone = ZoneId.systemDefault();
        var startOfToday = today.atStartOfDay(zone).toInstant();
        var startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(zone).toInstant();
        var startOfMonth = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();

        var metrics = METRIC_ACTIONS.stream().map(action -> {
            var total = auditLogQueryUseCases.countByAction(action);
            var todayCount = auditLogQueryUseCases.countByActionBetween(action, startOfToday, now);
            var weekCount = auditLogQueryUseCases.countByActionBetween(action, startOfWeek, now);
            var monthCount = auditLogQueryUseCases.countByActionBetween(action, startOfMonth, now);
            return new MetricsResponse(action, total, todayCount, weekCount, monthCount);
        }).toList();

        cacheService.put(cacheKey, new CacheableMetricsList(metrics), Duration.ofSeconds(60));
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    record CacheableMetricsList(List<MetricsResponse> metrics) {}
}
