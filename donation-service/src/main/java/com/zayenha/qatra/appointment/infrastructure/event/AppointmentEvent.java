package com.zayenha.qatra.appointment.infrastructure.event;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;

public record AppointmentEvent(
    Long appointmentId,
    Long donorId,
    Long slotId,
    Long centerId,
    AppointmentStatus status,
    DonationOutcome outcome,
    String action
) {}
