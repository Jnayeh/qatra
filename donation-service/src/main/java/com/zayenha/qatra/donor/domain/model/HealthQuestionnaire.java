package com.zayenha.qatra.donor.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HealthQuestionnaire {
    private Long id;
    private Long donorId;
    private Boolean hasChronicIllness;
    private Instant lastSurgeryAt;
    private Instant lastTravelAt;
    private Instant lastTattooOrPiercingAt;
    private Boolean onMedication;
    private String medicalConditionsDetails;
    private String medicationDetails;
    private Instant createdAt;
    private Instant updatedAt;

    public HealthQuestionnaire() {}

    public HealthQuestionnaire(Long donorId) {
        this.donorId = donorId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
