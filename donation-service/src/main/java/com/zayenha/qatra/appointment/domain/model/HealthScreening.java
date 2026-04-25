package com.zayenha.qatra.appointment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HealthScreening {
    private Long id;
    private Long appointmentId;
    private Long donorId;
    private Double weight;
    private String bloodPressure;
    private Double hemoglobin;
    private Long screenedByStaffId;
    private Double temperature;
    private String notes;
    private Boolean eligible;
    private Instant screenedAt;

    public HealthScreening() {}

    public HealthScreening(Long appointmentId, Long donorId) {
        this.appointmentId = appointmentId;
        this.donorId = donorId;
    }
}
