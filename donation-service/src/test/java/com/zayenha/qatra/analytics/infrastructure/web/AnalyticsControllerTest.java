package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.in.AuditLogQueryUseCases;
import com.zayenha.qatra.analytics.infrastructure.mapper.AnalyticsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AuditLogQueryUseCases auditLogQueryUseCases;
    @Mock
    private AnalyticsMapper mapper;
    @Mock
    private CacheService cacheService;

    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsController(auditLogQueryUseCases, mapper, cacheService);
    }

    @Test
    void getAuditLogsReturnsPaginatedResults() {
        var log = new AuditLog();
        log.setId(1L);
        log.setAction("APPOINTMENT_CREATED");
        log.setTimestamp(Instant.now());
        var pageResult = new PageResult<AuditLog>(List.of(log), 0, 20, 1, 1);
        when(auditLogQueryUseCases.findFiltered(any(SearchCriteria.class), any(), any(), any(), any())).thenReturn(pageResult);

        var response = controller.getAuditLogs(0, 20, null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getMetricsReturnsEnrichedCounts() {
        when(cacheService.get(anyString(), any(Class.class))).thenReturn(Optional.empty());
        when(auditLogQueryUseCases.countByAction(anyString())).thenReturn(10L);
        when(auditLogQueryUseCases.countByActionBetween(anyString(), any(), any())).thenReturn(3L);

        var response = controller.getMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).isNotEmpty();
        response.getBody().data().forEach(m -> {
            assertThat(m.total()).isEqualTo(10L);
            assertThat(m.today()).isEqualTo(3L);
            assertThat(m.thisWeek()).isEqualTo(3L);
            assertThat(m.thisMonth()).isEqualTo(3L);
        });
    }

    @Test
    void getMetricsUsesCache() {
        var cachedMetrics = List.of(
            new com.zayenha.qatra.analytics.infrastructure.web.dto.response.MetricsResponse(
                "APPOINTMENT_CREATED", 50L, 5L, 20L, 40L)
        );
        when(cacheService.get(eq("metrics:overview"), any(Class.class))).thenReturn(
            Optional.of(new AnalyticsController.CacheableMetricsList(cachedMetrics)));

        var response = controller.getMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
        assertThat(response.getBody().data().get(0).total()).isEqualTo(50L);
    }
}
