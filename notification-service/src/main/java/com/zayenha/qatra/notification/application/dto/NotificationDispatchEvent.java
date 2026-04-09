package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationDispatchEvent(
    Long userId,
    String type,
    String title,
    String body,
    String data,
    String correlationId,
    Instant occurredAt
) {}
