package com.zayenha.qatra.appointment.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record RescheduleAppointmentRequest(
        @NotNull Long newSlotId
) {}
