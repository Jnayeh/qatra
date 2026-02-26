package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra._shared.domain.BloodType;

import java.time.Instant;

public record DonorProfileResponse(
    Long id,
    Long userId,
    BloodType bloodType,
    boolean bloodTypeVerified,
    Double latitude,
    Double longitude,
    String city,
    String country,
    AvailabilityStatus availabilityStatus,
    NotificationPreferences notificationPreferences,
    boolean permanentlyRestricted,
    String restrictionReason,
    boolean flaggedForManualReview,
    int reliabilityScore,
    Instant eligibleFromDate,
    boolean profileComplete,
    int totalDonations,
    int estimatedLivesSaved,
    Instant createdAt,
    Instant updatedAt
) {}
