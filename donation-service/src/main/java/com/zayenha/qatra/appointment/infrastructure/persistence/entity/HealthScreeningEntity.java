package com.zayenha.qatra.appointment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "health_screenings")
@Getter
@Setter
@NoArgsConstructor
public class HealthScreeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long appointmentId;

    private Double weight;
    private String bloodPressure;
    private Double hemoglobin;
    private Double temperature;

    @Column(nullable = false)
    private Boolean eligible;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
