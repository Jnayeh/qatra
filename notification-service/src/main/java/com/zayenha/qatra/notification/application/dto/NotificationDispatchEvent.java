package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationType;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationDispatchEvent(
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
