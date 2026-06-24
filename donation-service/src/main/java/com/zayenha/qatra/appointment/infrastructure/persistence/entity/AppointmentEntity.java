package com.zayenha.qatra.appointment.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
public class AppointmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq")
    @SequenceGenerator(name = "appointment_seq", sequenceName = "appointment_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfileEntity donor;

    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    private SlotEntity slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private CenterEntity center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id")
    private EmergencyRequestEntity emergency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completed_by_staff_id")
    private UserEntity completedByStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentType appointmentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private DonationOutcome outcome;

    private Integer mlCollected;

    private String qrCode;

    private Instant checkedInAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    private String cancellationReason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

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
