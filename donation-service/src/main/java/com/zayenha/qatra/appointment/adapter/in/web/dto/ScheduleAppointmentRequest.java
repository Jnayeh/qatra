package com.zayenha.qatra.appointment.adapter.in.web.dto;

import com.zayenha.qatra.appointment.domain.model.AppointmentType;
import jakarta.validation.constraints.NotNull;

public record ScheduleAppointmentRequest(
        @NotNull Long donorId,
        @NotNull Long centerId,
        @NotNull Long slotId,
        @NotNull AppointmentType appointmentType,
        Long emergencyId
) {}
