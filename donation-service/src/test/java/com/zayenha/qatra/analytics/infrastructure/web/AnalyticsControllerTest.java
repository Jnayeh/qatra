package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.analytics.application.AuditLogService;
import com.zayenha.qatra.analytics.domain.model.AuditLog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AuditLogService auditLogService;

    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsController(auditLogService);
    }

    @Test
    void getAuditLogsReturnsPaginatedResults() {
        var log = new AuditLog();
        log.setId(1L);
        log.setEventType("APPOINTMENT_CREATED");
        log.setTimestamp(Instant.now());
        var pageResult = new PageResult<AuditLog>(List.of(log), 0, 20, 1, 1);
        when(auditLogService.findAll(any(SearchCriteria.class))).thenReturn(pageResult);

        var response = controller.getAuditLogs(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getMetricsReturnsCounts() {
        when(auditLogService.countByEventType(anyString())).thenReturn(0L);

        var response = controller.getMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).isNotEmpty();
    }
}
