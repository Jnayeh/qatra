package com.zayenha.qatra.emergency.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class EmergencyRequest {
    private Long id;
    private Long centerId;
    private Long createdByStaffId;
    private BloodType bloodType;
    private Integer unitsNeeded;
    private EmergencyUrgency urgency;
    private String contactPhone;
    private EmergencyStatus status;
    private Integer matchRadius;
    private Integer escalationLevel;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant resolvedAt;
    private Long resolvedByUserId;

    public EmergencyRequest() {}

    public EmergencyRequest(Long centerId, Long createdByStaffId, BloodType bloodType,
                            Integer unitsNeeded, EmergencyUrgency urgency,
                            String contactPhone, Integer matchRadius, Instant expiresAt) {
        this.centerId = centerId;
        this.createdByStaffId = createdByStaffId;
        this.bloodType = bloodType;
        this.unitsNeeded = unitsNeeded;
        this.urgency = urgency;
        this.contactPhone = contactPhone;
        this.matchRadius = matchRadius;
        this.expiresAt = expiresAt;
        this.status = EmergencyStatus.OPEN;
        this.escalationLevel = 0;
        this.createdAt = Instant.now();
    }

    public void matchDonors() {}

    public void updateStatus(EmergencyStatus status) {
        this.status = status;
    }

    public void resolve(Long resolvedByUserId) {
        this.status = EmergencyStatus.FULFILLED;
        this.resolvedAt = Instant.now();
        this.resolvedByUserId = resolvedByUserId;
    }

    public void cancel() {
        this.status = EmergencyStatus.CANCELLED;
    }

    public void fulfill() {
        this.status = EmergencyStatus.FULFILLED;
    }
}
