package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;

import java.time.Instant;
import java.time.LocalDate;

public record DonorDetailResponse(
    Long id,
    Long userId,
    BloodType bloodType,
    Boolean bloodTypeVerified,
    Double latitude,
    Double longitude,
    String city,
    AvailabilityStatus availability,
    NotificationPreferences notificationPreferences,
    Boolean permanentlyRestricted,
    String restrictionReason,
    Boolean flaggedForManualReview,
    Double reliabilityScore,
    LocalDate eligibleFromDate,
    Boolean profileComplete,
    int totalDonations,
    DonorHealthResponse healthQuestionnaire,
    Instant createdAt,
    Instant updatedAt
) {}
