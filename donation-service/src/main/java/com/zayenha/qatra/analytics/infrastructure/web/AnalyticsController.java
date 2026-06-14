package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.analytics.domain.port.in.AuditLogQueryUseCases;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.AuditLogResponse;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.MetricsResponse;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.CenterMetricsResponse;
import com.zayenha.qatra.analytics.infrastructure.mapper.AnalyticsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.StringWriter;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long centerId) {
        var criteria = new SearchCriteria(null, "timestamp", "desc", page, size);
        var result = auditLogQueryUseCases.findFiltered(criteria, action, parseDate(fromDate), parseDate(toDate), centerId);
        return ResponseEntity.ok(ApiResponse.success(
            result.content().stream().map(mapper::toResponse).toList(),
            PageHelper.fromDomain(result)
        ));
    }

    @GetMapping("/audit-logs/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Long centerId) {
        var criteria = new SearchCriteria(null, "timestamp", "desc", 0, 10000);
        var result = auditLogQueryUseCases.findFiltered(criteria, action, parseDate(fromDate), parseDate(toDate), centerId);

        var csv = new StringWriter();
        csv.append("ID,User ID,Action,Entity Type,Entity ID,IP Address,Timestamp\n");
        for (var log : result.content()) {
            csv.append(String.valueOf(log.getId())).append(",");
            csv.append(String.valueOf(log.getUserId())).append(",");
            csv.append(log.getAction()).append(",");
            csv.append(log.getEntityType() != null ? log.getEntityType() : "").append(",");
            csv.append(log.getEntityId() != null ? String.valueOf(log.getEntityId()) : "").append(",");
            csv.append(log.getIpAddress() != null ? log.getIpAddress() : "").append(",");
            csv.append(log.getTimestamp() != null ? log.getTimestamp().toString() : "").append("\n");
        }

        return ResponseEntity.ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=\"audit-logs.csv\"")
            .body(csv.toString());
    }

    @GetMapping("/audit-logs/by-action/{action}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByAction(@PathVariable String action) {
        var logs = auditLogQueryUseCases.findByAction(action);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/audit-logs/by-user/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getByUser(@PathVariable Long userId) {
        var logs = auditLogQueryUseCases.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(
            logs.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
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

    @GetMapping("/centers/{centerId}/metrics")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<CenterMetricsResponse>> getCenterMetrics(@PathVariable Long centerId) {
        var cacheKey = "metrics:center:" + centerId;
        var cached = cacheService.get(cacheKey, CenterMetricsResponse.class);
        if (cached.isPresent()) return ResponseEntity.ok(ApiResponse.success(cached.get()));

        var domain = auditLogQueryUseCases.getCenterMetrics(centerId);

        var result = new CenterMetricsResponse(
            domain.totalAppointments(), domain.completedAppointments(),
            domain.todayAppointments(), domain.weekAppointments(), domain.monthAppointments(),
            domain.totalEmergencies(), domain.fulfilledEmergencies(),
            domain.todayEmergencies(), domain.weekEmergencies(), domain.monthEmergencies(),
            domain.totalDonorResponses(), domain.responseRate30d(),
            domain.totalMlCollected(), domain.activeEmergencies(),
            domain.appointmentsByDay(), domain.emergenciesByDay()
        );

        cacheService.put(cacheKey, result, Duration.ofSeconds(600));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    record CacheableMetricsList(List<MetricsResponse> metrics) {}

    private static Instant parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        return LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
    }
}
