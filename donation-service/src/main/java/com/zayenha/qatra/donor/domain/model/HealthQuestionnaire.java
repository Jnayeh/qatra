package com.zayenha.qatra.donor.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class HealthQuestionnaire {
    private Long id;
    private Long donorId;
    private boolean hasChronicIllness;
    private String medicalConditionsDetails;
    private boolean onMedication;
    private String medicationDetails;
    private boolean recentSurgery;
    private boolean recentTravel;
    private boolean recentTattooOrPiercing;
    private Instant createdAt;
    private Instant updatedAt;

    public HealthQuestionnaire() {}

    public HealthQuestionnaire(Long donorId) {
        this.donorId = donorId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
