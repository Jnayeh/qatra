package com.zayenha.qatra.donor.infrastructure.persistence.entity;

import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.shared.domain.BloodType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "donor_profiles")
@Getter
@Setter
@NoArgsConstructor
public class DonorProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Column(nullable = false)
    private boolean bloodTypeVerified;

    private Double latitude;
    private Double longitude;
    private String city;
    private String country;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availabilityStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    private NotificationPreferences notificationPreferences;

    @Column(nullable = false)
    private boolean permanentlyRestricted;

    private String restrictionReason;

    @Column(nullable = false)
    private boolean flaggedForManualReview;

    @Column(nullable = false)
    private int consecutiveEmergencyDeclines;

    @Column(nullable = false)
    private int reliabilityScore;

    private Instant eligibleFromDate;

    @Column(nullable = false)
    private boolean profileComplete;

    private Instant lastAcceptAt;

    @Column(nullable = false)
    private int totalDonations;

    @Column(nullable = false)
    private int estimatedLivesSaved;

    @Enumerated(EnumType.STRING)
    private DonorStatus status;

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
