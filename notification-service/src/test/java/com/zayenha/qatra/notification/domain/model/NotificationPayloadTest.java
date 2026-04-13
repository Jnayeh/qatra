package com.zayenha.qatra.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NotificationPayloadTest {

    @Test
    void shouldCreatePayload() {
        var now = Instant.now();
        var payload = new NotificationPayload(1L, "TEST", "Title", "Body", null, "corr-123", now);
        assertEquals(1L, payload.userId());
        assertEquals("TEST", payload.type());
        assertEquals("corr-123", payload.correlationId());
    }
}
