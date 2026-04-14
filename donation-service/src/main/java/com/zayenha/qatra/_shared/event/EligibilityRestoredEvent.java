package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibilityRestoredEvent(
    Long donorId,
    String eligibleFromDate,
    String correlationId,
    Instant occurredAt
) {}
