package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.application.proxy.GDPRUserProxy;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GDPRServiceTest {

    @Mock
    private GDPRRepositoryPort repository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AuditPublisher auditPublisher;
    @Mock
    private GDPRUserProxy userProxy;

    private GDPRService service;

    @BeforeEach
    void setUp() {
        service = new GDPRService(repository, eventPublisher, auditPublisher, userProxy);
    }

    @Test
    void requestDeletionCreatesInProgressRequest() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.requestDeletion(1L, "User request");

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(GDPRDeletionStatus.IN_PROGRESS);
    }

    @Test
    void completeUpdatesStatus() {
        var request = new GDPRDeletionRequest(1L, "User request");
        when(repository.findById(1L)).thenReturn(Optional.of(request));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = service.complete(1L);

        assertThat(result.getStatus()).isEqualTo(GDPRDeletionStatus.COMPLETED);
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
