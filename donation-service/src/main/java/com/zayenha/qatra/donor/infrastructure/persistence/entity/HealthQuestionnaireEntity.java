package com.zayenha.qatra.donor.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "health_questionnaires")
@Getter
@Setter
@NoArgsConstructor
public class HealthQuestionnaireEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "health_questionnaire_seq")
    @SequenceGenerator(name = "health_questionnaire_seq", sequenceName = "health_questionnaire_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false, unique = true)
    private DonorProfileEntity donor;

    @Column(nullable = false)
    private boolean hasChronicIllness;

    @Column(columnDefinition = "TEXT")
    private String medicalConditionsDetails;

    @Column(nullable = false)
    private boolean onMedication;

    @Column(columnDefinition = "TEXT")
    private String medicationDetails;

    private Instant lastSurgeryAt;
    private Instant lastTravelAt;
    private Instant lastTattooOrPiercingAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
