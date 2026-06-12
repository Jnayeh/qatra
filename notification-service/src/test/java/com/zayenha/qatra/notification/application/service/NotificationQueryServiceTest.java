package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.exception.NotificationNotFoundException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationQueryServiceTest {

    @Mock
    private NotificationRepositoryPort notificationRepository;

    private NotificationQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new NotificationQueryService(notificationRepository);
    }

    @Test
    void shouldReturnUserNotifications() {
        when(notificationRepository.findByUserId(eq(1L), isNull(), isNull(), eq(0), eq(20)))
                .thenReturn(List.of(new Notification(1L, null, null, null,
                        NotificationType.GENERAL, "T", "B", null, "c1", List.of(NotificationChannel.IN_APP))));

        var result = queryService.getUserNotifications(1L, null, null, 0, 20);
        assertEquals(1, result.size());
    }

    @Test
    void shouldCountUnread() {
        when(notificationRepository.countUnreadByUserId(1L)).thenReturn(5L);

        var count = queryService.countUnread(1L);
        assertEquals(5L, count);
    }

    @Test
    void shouldMarkAsRead() {
        var notification = new Notification(1L, null, null, null,
                NotificationType.GENERAL, "T", "B", null, "c1", List.of(NotificationChannel.IN_APP));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = queryService.markAsRead(1L, 1L);
        assertEquals("READ", response.status());
        assertNotNull(response.readAt());
    }

    @Test
    void shouldThrowWhenMarkingNonExistent() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotificationNotFoundException.class, () -> queryService.markAsRead(99L, 1L));
    }

    @Test
    void shouldMarkAllAsRead() {
        when(notificationRepository.markAllAsReadByUserId(1L)).thenReturn(3);

        var count = queryService.markAllAsRead(1L);
        assertEquals(3, count);
    }
}
