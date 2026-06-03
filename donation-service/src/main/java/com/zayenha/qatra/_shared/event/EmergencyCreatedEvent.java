package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EmergencyCreatedEvent(
    Long emergencyId,
    List<Long> matchedUserIds,
    String correlationId,
    Instant occurredAt,
    List<String> channels
) {}
