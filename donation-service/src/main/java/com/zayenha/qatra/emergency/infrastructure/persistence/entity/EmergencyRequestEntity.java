package com.zayenha.qatra.emergency.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "emergency_requests")
@Getter
@Setter
@NoArgsConstructor
public class EmergencyRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Column(nullable = false)
    private Integer unitsNeeded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyUrgency urgency;

    @Column(nullable = false)
    private String hospital;

    private String hospitalAddress;
    private Double latitude;
    private Double longitude;
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private Instant expiresAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
