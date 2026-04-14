package com.zayenha.qatra._shared.event;

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
