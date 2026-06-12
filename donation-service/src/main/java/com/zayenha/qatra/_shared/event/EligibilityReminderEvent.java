package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibilityReminderEvent(
    Long userId,
    String eligibleFromDate,
    String correlationId,
    Instant occurredAt,
    List<String> channels
) {}
