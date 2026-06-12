package com.zayenha.qatra.notification.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibilityRestoredEvent(
    Long userId,
    String eligibleFromDate,
    String correlationId,
    Instant occurredAt,
    List<String> channels
) {}
