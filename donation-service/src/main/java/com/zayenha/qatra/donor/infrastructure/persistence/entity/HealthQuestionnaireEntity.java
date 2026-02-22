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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long donorId;

    @Column(nullable = false)
    private boolean hasChronicIllness;

    @Column(columnDefinition = "TEXT")
    private String medicalConditionsDetails;

    @Column(nullable = false)
    private boolean onMedication;

    @Column(columnDefinition = "TEXT")
    private String medicationDetails;

    @Column(nullable = false)
    private boolean recentSurgery;

    @Column(nullable = false)
    private boolean recentTravel;

    @Column(nullable = false)
    private boolean recentTattooOrPiercing;

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
