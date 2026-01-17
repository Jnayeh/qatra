package com.zayenha.qatra.appointment.adapter.in.web.dto;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAppointmentStatusRequest(
        @NotNull AppointmentStatus status,
        String reason,
        Integer mlCollected,
        Long completedByStaffId
) {}
