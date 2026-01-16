package com.zayenha.qatra.appointment.domain.model.vo;

public record AppointmentId(Long value) {
    public AppointmentId {
        if (value != null && value < 0) {
            throw new IllegalArgumentException("AppointmentId must not be negative");
        }
    }
}
