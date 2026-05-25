package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationResultEvent(
    String correlationId,
    String originalEventType,
    Long userId,
    String status,
    String errorMessage,
    Instant occurredAt
) {}
