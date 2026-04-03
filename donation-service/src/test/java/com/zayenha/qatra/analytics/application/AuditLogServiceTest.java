package com.zayenha.qatra.analytics.application;

import com.zayenha.qatra.analytics.domain.model.AuditLog;
import com.zayenha.qatra.analytics.domain.port.out.AuditLogRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepositoryPort repository;

    private AuditLogService service;

    @BeforeEach
    void setUp() {
        service = new AuditLogService(repository);
    }

    @Test
    void recordSavesAuditLog() {
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.record(1L, "APPOINTMENT_CREATED", "Appointment", 10L, null, "{}", null, null);

        verify(repository).save(any());
    }

    @Test
    void countByActionReturnsCount() {
        when(repository.countByAction("APPOINTMENT_CREATED")).thenReturn(5L);

        assertThat(service.countByAction("APPOINTMENT_CREATED")).isEqualTo(5L);
    }
}
