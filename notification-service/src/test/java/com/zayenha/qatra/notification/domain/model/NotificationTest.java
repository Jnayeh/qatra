package com.zayenha.qatra.notification.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void shouldCreatePendingNotification() {
        var notification = new Notification(1L, "APPOINTMENT_REMINDER", "Title", "Body", null, "corr-123");
        assertEquals(1L, notification.getUserId());
        assertEquals("APPOINTMENT_REMINDER", notification.getType());
        assertEquals(NotificationStatus.PENDING, notification.getStatus());
        assertNotNull(notification.getCreatedAt());
    }

    @Test
    void shouldMarkAsSent() {
        var notification = new Notification(1L, "TEST", "Title", "Body", null, "corr-456");
        notification.markSent();
        assertEquals(NotificationStatus.SENT, notification.getStatus());
    }

    @Test
    void shouldMarkAsRead() {
        var notification = new Notification(1L, "TEST", "Title", "Body", null, "corr-789");
        notification.markRead();
        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
    }

    @Test
    void shouldMarkAsFailed() {
        var notification = new Notification(1L, "TEST", "Title", "Body", null, "corr-000");
        notification.markFailed();
        assertEquals(NotificationStatus.FAILED, notification.getStatus());
    }
}
