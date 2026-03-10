package com.zayenha.qatra.emergency.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EmergencyRequest {
    private Long id;
    private String patientName;
    private BloodType bloodType;
    private Integer unitsNeeded;
    private EmergencyUrgency urgency;
    private String hospital;
    private String hospitalAddress;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private EmergencyStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant expiresAt;

    public EmergencyRequest() {}

    public EmergencyRequest(String patientName, BloodType bloodType, Integer unitsNeeded,
                            EmergencyUrgency urgency, String hospital, Double latitude, Double longitude,
                            String contactPhone) {
        this.patientName = patientName;
        this.bloodType = bloodType;
        this.unitsNeeded = unitsNeeded;
        this.urgency = urgency;
        this.hospital = hospital;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactPhone = contactPhone;
        this.status = EmergencyStatus.OPEN;
    }

    public void cancel() { this.status = EmergencyStatus.CANCELLED; }
    public void fulfill() { this.status = EmergencyStatus.FULFILLED; }
}
