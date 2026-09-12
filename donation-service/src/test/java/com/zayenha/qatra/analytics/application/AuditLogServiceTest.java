package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra.analytics.application.proxy.AnalyticsCenterProxy;
import com.zayenha.qatra.analytics.application.proxy.AnalyticsEmergencyProxy;
import com.zayenha.qatra.analytics.application.proxy.AnalyticsUserProxy;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import com.zayenha.qatra.analytics.domain.port.out.CenterMetricsRepositoryPort;
import com.zayenha.qatra._shared.domain.enums.LogAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepositoryPort repository;
    @Mock
    private CenterMetricsRepositoryPort centerMetricsRepo;
    @Mock
    private AnalyticsUserProxy userProxy;
    @Mock
    private AnalyticsCenterProxy centerProxy;
    @Mock
    private AnalyticsEmergencyProxy emergencyProxy;

    private AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(repository, centerMetricsRepo, userProxy, centerProxy, emergencyProxy);
    }

    @Test
    void recordSavesAuditLog() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.record(1L, LogAction.APPOINTMENT_BOOKED, "Appointment", 10L, null, null, null);

        verify(repository).save(any());
    }

    @Test
    void countByActionReturnsCount() {
        when(repository.countByAction(LogAction.APPOINTMENT_BOOKED)).thenReturn(5L);

        assertThat(service.countByAction(LogAction.APPOINTMENT_BOOKED)).isEqualTo(5L);
    }

    @Test
    void countByActionBetweenReturnsCountInRange() {
        var from = Instant.parse("2026-01-01T00:00:00Z");
        var to = Instant.parse("2026-02-01T00:00:00Z");
        when(repository.countByActionBetween(LogAction.APPOINTMENT_BOOKED, from, to)).thenReturn(3L);

        assertThat(service.countByActionBetween(LogAction.APPOINTMENT_BOOKED, from, to)).isEqualTo(3L);
    }
}
