package com.zayenha.qatra.notification.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NotificationPayload(
    Long userId,
    String email,
    Long emergencyId,
    Long appointmentId,
    NotificationType type,
    String title,
    String body,
    String htmlBody,
    Map<String, Object> data,
    String correlationId,
    Instant occurredAt,
    List<NotificationChannel> requestedChannels
) {}
