package com.zayenha.qatra.notification.application.dto;

import java.time.Instant;

public record NotificationResponse(
    Long id,
    Long userId,
    String type,
    String title,
    String body,
    String data,
    String status,
    Instant createdAt,
    Instant readAt
) {}
