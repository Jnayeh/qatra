package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppointmentReminderEvent(
    Long appointmentId,
    Long donorId,
    String slotTime,
    String correlationId,
    Instant occurredAt
) {}
