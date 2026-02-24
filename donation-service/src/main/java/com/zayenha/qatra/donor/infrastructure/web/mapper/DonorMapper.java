package com.zayenha.qatra.donor.infrastructure.web.mapper;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorProfileResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityResponse;

import java.time.Instant;

public class DonorMapper {

    public static DonorProfileResponse toProfileResponse(DonorProfile profile) {
        return new DonorProfileResponse(
            profile.getId(), profile.getUserId(),
            profile.getBloodType(), profile.isBloodTypeVerified(),
            profile.getLatitude(), profile.getLongitude(),
            profile.getCity(), profile.getCountry(),
            profile.getAvailabilityStatus(),
            profile.getNotificationPreferences(),
            profile.isPermanentlyRestricted(), profile.getRestrictionReason(),
            profile.isFlaggedForManualReview(), profile.getReliabilityScore(),
            profile.getEligibleFromDate(), profile.isProfileComplete(),
            profile.getTotalDonations(), profile.getEstimatedLivesSaved(),
            profile.getCreatedAt(), profile.getUpdatedAt()
        );
    }

    public static DonorHealthResponse toHealthResponse(HealthQuestionnaire q) {
        return new DonorHealthResponse(
            q.getId(), q.getDonorId(), q.isHasChronicIllness(),
            q.getMedicalConditionsDetails(), q.isOnMedication(),
            q.getMedicationDetails(), q.isRecentSurgery(),
            q.isRecentTravel(), q.isRecentTattooOrPiercing(),
            q.getCreatedAt(), q.getUpdatedAt()
        );
    }

    public static EligibilityResponse toEligibilityResponse(DonorProfile profile) {
        var now = Instant.now();
        var eligible = profile.getEligibleFromDate() == null || !now.isBefore(profile.getEligibleFromDate());
        String reason = null;
        if (!eligible) {
            reason = "Cooldown period active until " + profile.getEligibleFromDate();
        }
        if (profile.isPermanentlyRestricted()) {
            eligible = false;
            reason = profile.getRestrictionReason();
        }
        return new EligibilityResponse(eligible, profile.getEligibleFromDate(), reason);
    }
}
