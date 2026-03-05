package com.zayenha.qatra.appointment.infrastructure.web.dto.response;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
    Long id,
    Long donorId,
    Long slotId,
    Long centerId,
    AppointmentStatus status,
    LocalDate appointmentDate,
    LocalTime startTime,
    LocalTime endTime,
    Instant checkInTime,
    Instant completedAt,
    DonationOutcome outcome,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
