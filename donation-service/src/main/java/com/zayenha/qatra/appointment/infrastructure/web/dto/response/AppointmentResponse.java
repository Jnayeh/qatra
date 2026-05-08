package com.zayenha.qatra.appointment.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;

import java.time.Instant;

public record AppointmentResponse(
    Long id,
    Long donorId,
    Long slotId,
    Long centerId,
    Long emergencyId,
    Long completedByStaffId,
    AppointmentType appointmentType,
    AppointmentStatus status,
    BloodType bloodType,
    DonationOutcome outcome,
    Integer mlCollected,
    String qrCode,
    Instant checkedInAt,
    Instant startedAt,
    Instant completedAt,
    Instant cancelledAt,
    String cancellationReason,
    String notes,
    Instant createdAt,
    Instant updatedAt
) {}
