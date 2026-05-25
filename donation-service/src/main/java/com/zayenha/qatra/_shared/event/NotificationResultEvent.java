package com.zayenha.qatra._shared.event;

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
