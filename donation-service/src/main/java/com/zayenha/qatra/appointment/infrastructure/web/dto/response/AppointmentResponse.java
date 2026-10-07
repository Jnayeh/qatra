package com.zayenha.qatra.appointment.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.DonationOutcome;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        Long donorId,
        Long slotId,
        Long centerId,
        Long emergencyId,
        Long completedByStaffId,
        LocalDate slotDate,
        LocalTime slotTime,
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
        Instant updatedAt,
        String donorFirstName,
        String donorLastName,
        String donorDisplayName,
        String donorPhone,
        String donorEmail,
        String donorCity,
        String slotName
) {}
