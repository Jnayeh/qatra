package com.zayenha.qatra.donor.infrastructure.mapper;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorDetailResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorProfileResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityDetailResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.ImpactResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.time.Instant;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DonorMapper {

    DonorProfileResponse toProfileResponse(DonorProfile profile);

    DonorHealthResponse toHealthResponse(HealthQuestionnaire questionnaire);

    ImpactResponse toImpactResponse(DonorQueryUseCases.ImpactResult result);

    default EligibilityResponse toEligibilityResponse(DonorProfile profile) {
        var now = Instant.now();
        var eligible = profile.getEligibleFromDate() == null
            || !now.isBefore(profile.getEligibleFromDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        String reason = null;
        if (!eligible) {
            reason = "Cooldown period active until " + profile.getEligibleFromDate();
        }
        if (Boolean.TRUE.equals(profile.getPermanentlyRestricted())) {
            eligible = false;
            reason = profile.getRestrictionReason();
        }
        return new EligibilityResponse(eligible, profile.getEligibleFromDate(), reason);
    }

    default EligibilityDetailResponse toEligibilityDetailResponse(DonorProfile profile) {
        var now = Instant.now();
        var eligible = profile.getEligibleFromDate() == null
            || !now.isBefore(profile.getEligibleFromDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        if (Boolean.TRUE.equals(profile.getPermanentlyRestricted())) {
            eligible = false;
        }
        return new EligibilityDetailResponse(eligible, profile.getEligibleFromDate(),
            profile.getPermanentlyRestricted(), profile.getRestrictionReason());
    }

    default DonorDetailResponse toDetailResponse(DonorProfile profile) {
        return new DonorDetailResponse(
            profile.getId(), profile.getUserId(),
            profile.getBloodType(), profile.getBloodTypeVerified(),
            profile.getLatitude(), profile.getLongitude(), profile.getCity(),
            profile.getAvailability(), profile.getNotificationPreferences(),
            profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
            profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
            profile.getEligibleFromDate(), profile.getProfileComplete(),
            profile.getTotalDonations(),
            null, 0,
            profile.getCreatedAt(), profile.getUpdatedAt()
        );
    }
}
