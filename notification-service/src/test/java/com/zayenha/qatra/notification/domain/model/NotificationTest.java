package com.zayenha.qatra.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreatePendingNotification() {
        var notification = new Notification(1L, null, null, null, NotificationType.APPOINTMENT_REMINDER, "Title", "Body", null, "corr-123", List.of(NotificationChannel.IN_APP));
        assertEquals(1L, notification.getUserId());
        assertEquals(NotificationType.APPOINTMENT_REMINDER, notification.getType());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void shouldMarkAsSent() {
        var notification = new Notification(1L, null, null, null, NotificationType.GENERAL, "Title", "Body", null, "corr-456", List.of(NotificationChannel.IN_APP));
        notification.markSent();
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void shouldMarkAsRead() {
        var notification = new Notification(1L, null, null, null, NotificationType.GENERAL, "Title", "Body", null, "corr-789", List.of(NotificationChannel.IN_APP));
        notification.markRead();
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void shouldMarkAsFailed() {
        var notification = new Notification(1L, null, null, null, NotificationType.GENERAL, "Title", "Body", null, "corr-000", List.of(NotificationChannel.IN_APP));
        notification.markFailed();
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
    }
}
