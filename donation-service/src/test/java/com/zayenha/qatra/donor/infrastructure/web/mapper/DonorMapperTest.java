package com.zayenha.qatra.donor.infrastructure.web.mapper;

import com.zayenha.qatra.donor.domain.model.*;
import com.zayenha.qatra._shared.domain.BloodType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DonorMapperTest {

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);
        profile.setLatitude(40.71);
        profile.setLongitude(-74.00);
        profile.setCity("NYC");
        profile.setCountry("USA");
        profile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        profile.setNotificationPreferences(new NotificationPreferences(
                NotificationFrequency.IMMEDIATE, null, true, 25));
        profile.setPermanentlyRestricted(false);
        profile.setFlaggedForManualReview(false);
        profile.setReliabilityScore(80);
        profile.setProfileComplete(true);
        profile.setTotalDonations(5);
        profile.setEstimatedLivesSaved(15);
        profile.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        profile.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));
        return profile;
    }

    @Test
    void toProfileResponseMapsAllFields() {
        var profile = aProfile();
        var response = DonorMapper.toProfileResponse(profile);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.bloodType()).isEqualTo(BloodType.A_POSITIVE);
        assertThat(response.bloodTypeVerified()).isTrue();
        assertThat(response.latitude()).isEqualTo(40.71);
        assertThat(response.longitude()).isEqualTo(-74.00);
        assertThat(response.city()).isEqualTo("NYC");
        assertThat(response.country()).isEqualTo("USA");
        assertThat(response.availabilityStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
        assertThat(response.notificationPreferences()).isNotNull();
        assertThat(response.permanentlyRestricted()).isFalse();
        assertThat(response.flaggedForManualReview()).isFalse();
        assertThat(response.reliabilityScore()).isEqualTo(80);
        assertThat(response.profileComplete()).isTrue();
        assertThat(response.totalDonations()).isEqualTo(5);
        assertThat(response.estimatedLivesSaved()).isEqualTo(15);
        assertThat(response.createdAt()).isEqualTo("2025-01-01T00:00:00Z");
        assertThat(response.updatedAt()).isEqualTo("2025-06-01T00:00:00Z");
    }

    @Test
    void toHealthResponseMapsAllFields() {
        var q = new HealthQuestionnaire(10L);
        q.setId(100L);
        q.setHasChronicIllness(true);
        q.setMedicalConditionsDetails("Diabetes");
        q.setOnMedication(true);
        q.setMedicationDetails("Insulin");
        q.setRecentSurgery(false);
        q.setRecentTravel(true);
        q.setRecentTattooOrPiercing(false);
        q.setCreatedAt(Instant.parse("2025-03-01T00:00:00Z"));
        q.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));

        var response = DonorMapper.toHealthResponse(q);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.hasChronicIllness()).isTrue();
        assertThat(response.medicalConditionsDetails()).isEqualTo("Diabetes");
        assertThat(response.onMedication()).isTrue();
        assertThat(response.medicationDetails()).isEqualTo("Insulin");
        assertThat(response.recentSurgery()).isFalse();
        assertThat(response.recentTravel()).isTrue();
        assertThat(response.recentTattooOrPiercing()).isFalse();
    }

    @Test
    void toEligibilityResponseReturnsEligibleWhenNoCooldown() {
        var profile = aProfile();
        var response = DonorMapper.toEligibilityResponse(profile);

        assertThat(response.eligible()).isTrue();
    }

    @Test
    void toEligibilityResponseReturnsNotEligibleWhenRestricted() {
        var profile = aProfile();
        profile.setPermanentlyRestricted(true);
        profile.setRestrictionReason("Chronic illness");

        var response = DonorMapper.toEligibilityResponse(profile);

        assertThat(response.eligible()).isFalse();
        assertThat(response.reason()).contains("Chronic illness");
    }
}
