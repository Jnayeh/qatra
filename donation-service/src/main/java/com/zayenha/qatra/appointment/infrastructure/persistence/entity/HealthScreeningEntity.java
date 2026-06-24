package com.zayenha.qatra.appointment.infrastructure.persistence.entity;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "health_screening_seq")
    @SequenceGenerator(name = "health_screening_seq", sequenceName = "health_screening_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", nullable = false)
    private AppointmentEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfileEntity donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screened_by_staff_id", nullable = false)
    private UserEntity screenedByStaff;

    private Double weight;
    private String bloodPressure;
    private Double hemoglobin;
    private Double temperature;

    @Column(nullable = false)
    private Boolean eligible;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant screenedAt;

    @PrePersist
    void onCreate() {
        if (screenedAt == null) screenedAt = Instant.now();
    }
}
