package com.zayenha.qatra.donor.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class DonorProfile {
    private Long id;
    private Long userId;
    private BloodType bloodType;
    private Boolean bloodTypeVerified;
    private Boolean profileComplete;
    private DonorStatus status;
    private Double latitude;
    private Double longitude;
    private String city;
    private AvailabilityStatus availability;
    private LocalDate lastDonationDate;
    private LocalDate eligibleFromDate;
    private NotificationPreferences notificationPreferences;
    private Boolean allowEmergencyNotifications;
    private Integer consecutiveEmergencyDeclines;
    private Boolean flaggedForManualReview;
    private Boolean permanentlyRestricted;
    private String restrictionReason;
    private Double reliabilityScore;
    private int totalDonations;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletionRequestedAt;
    private Instant deletedAt;
    private Instant lastAcceptAt;

    public DonorProfile() {}

    public DonorProfile(Long userId) {
        this.userId = userId;
        this.bloodType = BloodType.UNKNOWN;
        this.bloodTypeVerified = false;
        this.profileComplete = false;
        this.status = DonorStatus.ACTIVE;
        this.availability = AvailabilityStatus.AVAILABLE;
        this.notificationPreferences = new NotificationPreferences(
            NotificationFrequency.IMMEDIATE, null, true, 25
        );
        this.allowEmergencyNotifications = true;
        this.consecutiveEmergencyDeclines = 0;
        this.flaggedForManualReview = false;
        this.permanentlyRestricted = false;
        this.reliabilityScore = 100.0;
        this.totalDonations = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean canDonate() {
        return !permanentlyRestricted
            && status == DonorStatus.ACTIVE
            && (eligibleFromDate == null || !Instant.now().isBefore(eligibleFromDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant()));
    }

    public void calculateEligibility() {
        // eligibility logic delegated to service
    }

    public void updateLocation(Double latitude, Double longitude, String city) {
        this.latitude = latitude;
        this.longitude = longitude;
        if (latitude != null && longitude != null) {
            this.city = city;
        }
    }

    public void resetConsecutiveDeclinesOnAccept() {
        this.consecutiveEmergencyDeclines = 0;
        this.lastAcceptAt = Instant.now();
    }
}
