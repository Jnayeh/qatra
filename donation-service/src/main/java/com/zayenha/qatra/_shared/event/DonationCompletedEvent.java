package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.zayenha.qatra._shared.domain.enums.AppointmentType;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DonationCompletedEvent(
    Long appointmentId,
    Long donorId,
    String donorName,
    Long centerId,
    String centerName,
    Integer mlCollected,
    AppointmentType donationType,
    Instant completedAt,
    String correlationId,
    Instant occurredAt
) {}
