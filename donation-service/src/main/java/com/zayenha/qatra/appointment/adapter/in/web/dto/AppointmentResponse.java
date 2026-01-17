package com.zayenha.qatra.appointment.adapter.in.web.dto;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.AppointmentType;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        Long donorId,
        Long centerId,
        Long slotId,
        Long emergencyId,
        AppointmentStatus status,
        AppointmentType appointmentType,
        Integer mlCollected,
        String notes,
        String cancellationReason,
        String qrCode,
        Long completedByStaffId,
        Instant createdAt,
        Instant confirmedAt,
        Instant completedAt,
        Instant cancelledAt
) {
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId() != null ? appointment.getId().value() : null,
                appointment.getDonorId(),
                appointment.getCenterId(),
                appointment.getSlotId(),
                appointment.getEmergencyId(),
                appointment.getStatus(),
                appointment.getAppointmentType(),
                appointment.getMlCollected(),
                appointment.getNotes(),
                appointment.getCancellationReason(),
                appointment.getQrCode(),
                appointment.getCompletedByStaffId(),
                appointment.getCreatedAt(),
                appointment.getConfirmedAt(),
                appointment.getCompletedAt(),
                appointment.getCancelledAt()
        );
    }
}
