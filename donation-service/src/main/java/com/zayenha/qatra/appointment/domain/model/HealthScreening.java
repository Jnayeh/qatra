package com.zayenha.qatra.appointment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HealthScreening {
    private Long id;
    private Long appointmentId;
    private Double weight;
    private String bloodPressure;
    private Double hemoglobin;
    private Double temperature;
    private Boolean eligible;
    private String notes;
    private Instant createdAt;

    public HealthScreening() {}

    public HealthScreening(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
}
