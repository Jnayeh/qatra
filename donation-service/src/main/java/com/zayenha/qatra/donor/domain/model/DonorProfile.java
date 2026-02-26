package com.zayenha.qatra.donor.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DonorProfile {
    private Long id;
    private Long userId;
    private BloodType bloodType;
    private boolean bloodTypeVerified;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;
    private AvailabilityStatus availabilityStatus;
    private NotificationPreferences notificationPreferences;
    private boolean permanentlyRestricted;
    private String restrictionReason;
    private boolean flaggedForManualReview;
    private int consecutiveEmergencyDeclines;
    private int reliabilityScore;
    private Instant eligibleFromDate;
    private boolean profileComplete;
    private Instant lastAcceptAt;
    private int totalDonations;
    private int estimatedLivesSaved;
    private DonorStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public DonorProfile() {}

    public DonorProfile(Long userId) {
        this.userId = userId;
        this.bloodType = BloodType.UNKNOWN;
        this.bloodTypeVerified = false;
        this.availabilityStatus = AvailabilityStatus.AVAILABLE;
        this.notificationPreferences = new NotificationPreferences(
            NotificationFrequency.IMMEDIATE, null, true, 25
        );
        this.permanentlyRestricted = false;
        this.flaggedForManualReview = false;
        this.consecutiveEmergencyDeclines = 0;
        this.reliabilityScore = 50;
        this.profileComplete = false;
        this.totalDonations = 0;
        this.estimatedLivesSaved = 0;
        this.status = DonorStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
