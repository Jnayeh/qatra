package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileCompletionEvent(
    Long userId,
    String email,
    Long profileId,
    String message,
    String correlationId,
    Instant occurredAt
) {}