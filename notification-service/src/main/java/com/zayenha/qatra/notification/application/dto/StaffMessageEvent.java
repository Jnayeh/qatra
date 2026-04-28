package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StaffMessageEvent(
    Long userId,
    String email,
    Long staffId,
    String subject,
    String body,
    String correlationId,
    Instant occurredAt
) {}