package com.zayenha.qatra.donor.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "donor_profiles")
@Getter
@Setter
@NoArgsConstructor
public class DonorProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "donor_profile_seq")
    @SequenceGenerator(name = "donor_profile_seq", sequenceName = "donor_profile_seq", allocationSize = 2, initialValue = 2)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Column(nullable = false)
    private Boolean bloodTypeVerified;

    @Column(nullable = false)
    private Boolean profileComplete;

    @Enumerated(EnumType.STRING)
    private DonorStatus status;

    private Double latitude;
    private Double longitude;
    private String city;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus availability;

    private LocalDate lastDonationDate;
    private LocalDate eligibleFromDate;

    @JdbcTypeCode(SqlTypes.JSON)
    private NotificationPreferences notificationPreferences;

    @Column(nullable = false)
    private Boolean allowEmergencyNotifications;

    @Column(nullable = false)
    private Integer consecutiveEmergencyDeclines;

    @Column(nullable = false)
    private Boolean flaggedForManualReview;

    @Column(nullable = false)
    private Boolean permanentlyRestricted;

    private String restrictionReason;

    @Column(nullable = false)
    private Double reliabilityScore;

    @Column(nullable = false)
    private int totalDonations;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deletion_requested_at")
    private Instant deletionRequestedAt;

    private Instant lastAcceptAt;

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
