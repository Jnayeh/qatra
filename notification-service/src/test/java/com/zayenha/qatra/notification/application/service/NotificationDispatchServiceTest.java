package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;
    @Mock
    private ChannelHandler inAppChannel;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        lenient().when(inAppChannel.type()).thenReturn(com.zayenha.qatra.notification.domain.model.NotificationChannel.IN_APP);
        dispatchService = new NotificationDispatchService(
                notificationRepository, List.of(inAppChannel), 3, 2000);
    }

    @Test
    void shouldSkipDuplicateByCorrelationId() {
        when(notificationRepository.existsByCorrelationId("dup-corr")).thenReturn(true);

        var payload = new NotificationPayload(1L, null, null, null, null, null, "Title", "Body", null, "dup-corr", Instant.now());
        dispatchService.dispatch(payload, "IN_APP");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void shouldDispatchToMatchingChannel() {
        when(notificationRepository.existsByCorrelationId(any())).thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var payload = new NotificationPayload(1L, null, null, null, null, null, "Title", "Body", null, "corr-1", Instant.now());
        dispatchService.dispatch(payload, "IN_APP");

        verify(notificationRepository, times(2)).save(any()); // save + update status
        verify(inAppChannel).deliver(payload);
    }

    @Test
    void shouldSkipNonConfiguredChannel() {
        when(notificationRepository.existsByCorrelationId(any())).thenReturn(false);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var payload = new NotificationPayload(1L, null, null, null, null, null, "Title", "Body", null, "corr-2", Instant.now());
        dispatchService.dispatch(payload, "EMAIL");

        verify(inAppChannel, never()).deliver(any());
    }
}
