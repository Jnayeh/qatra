package com.zayenha.qatra.notification.application.dto;

import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record NotificationResponse(
    Long id,
    Long userId,
    Long emergencyId,
    Long appointmentId,
    NotificationType type,
    List<NotificationChannel> channels,
    String title,
    String body,
    Map<String, Object> data,
    String status,
    Instant createdAt,
    Instant sentAt,
    Instant readAt
) {}
