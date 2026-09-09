package com.zayenha.qatra.appointment.infrastructure.event;

import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.DonationOutcome;

public record AppointmentEvent(
    Long appointmentId,
    Long donorId,
    Long slotId,
    Long centerId,
    AppointmentStatus status,
    DonationOutcome outcome,
    String action
) {}
