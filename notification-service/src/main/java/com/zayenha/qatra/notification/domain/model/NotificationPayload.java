package com.zayenha.qatra.notification.domain.model;

import java.time.Instant;
import java.util.Map;

public record NotificationPayload(
    Long userId,
    String email,
    Long emergencyId,
    Long appointmentId,
    NotificationType type,
    NotificationChannel channel,
    String title,
    String body,
    Map<String, Object> data,
    String correlationId,
    Instant occurredAt
) {}
