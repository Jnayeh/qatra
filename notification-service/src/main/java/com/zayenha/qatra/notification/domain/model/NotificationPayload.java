package com.zayenha.qatra.notification.domain.model;

import java.time.Instant;

public record NotificationPayload(
    Long userId,
    String type,
    String title,
    String body,
    String data,
    String correlationId,
    Instant occurredAt
) {}
