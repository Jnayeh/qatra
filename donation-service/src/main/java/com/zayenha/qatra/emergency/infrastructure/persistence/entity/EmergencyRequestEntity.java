package com.zayenha.qatra.emergency.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emergency_request_seq")
    @SequenceGenerator(name = "emergency_request_seq", sequenceName = "emergency_request_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private CenterEntity center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_staff_id", nullable = false)
    private UserEntity createdByStaff;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Column(nullable = false)
    private Integer unitsNeeded;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyUrgency urgency;

    @Column(nullable = false)
    private Integer matchRadius;

    @Column(nullable = false)
    private Integer escalationLevel;

    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyStatus status;

    private Instant resolvedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private UserEntity resolvedBy;

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
