package com.zayenha.qatra.analytics.infrastructure.web;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AuditLogQueryUseCases auditLogQueryUseCases;
    @Mock
    private AnalyticsMapper mapper;

    private AnalyticsController controller;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsController(auditLogQueryUseCases, mapper);
    }

    @Test
    void getAuditLogsReturnsPaginatedResults() {
        var log = new AuditLog();
        log.setId(1L);
        log.setAction("APPOINTMENT_CREATED");
        log.setTimestamp(Instant.now());
        var pageResult = new PageResult<AuditLog>(List.of(log), 0, 20, 1, 1);
        when(auditLogQueryUseCases.findAll(any(SearchCriteria.class))).thenReturn(pageResult);

        var response = controller.getAuditLogs(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getMetricsReturnsCounts() {
        when(auditLogQueryUseCases.countByAction(anyString())).thenReturn(0L);

        var response = controller.getMetrics();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).isNotEmpty();
    }
}
