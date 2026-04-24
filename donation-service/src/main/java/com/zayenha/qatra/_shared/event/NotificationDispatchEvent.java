package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NotificationDispatchEvent(
    Long userId,
    String email,
    String type,
    String title,
    String body,
    Map<String, Object> data,
    String correlationId,
    Instant occurredAt
) {}
