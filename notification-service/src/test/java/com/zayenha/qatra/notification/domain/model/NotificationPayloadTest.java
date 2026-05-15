package com.zayenha.qatra.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPayloadTest {

    @Test
    void shouldCreatePayload() {
        var now = Instant.now();
        var payload = new NotificationPayload(1L, null, null, null, NotificationType.GENERAL, null, "Title", "Body", null, "corr-123", now);
        assertEquals(1L, payload.userId());
        assertEquals(NotificationType.GENERAL, payload.type());
        assertEquals("corr-123", payload.correlationId());
    }
}
