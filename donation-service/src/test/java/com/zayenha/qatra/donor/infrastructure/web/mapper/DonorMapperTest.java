package com.zayenha.qatra.donor.infrastructure.web.mapper;

import com.zayenha.qatra.donor.domain.model.*;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapper;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapperImpl;
import com.zayenha.qatra._shared.domain.BloodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DonorMapperTest {

    private DonorMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DonorMapperImpl();
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);
        profile.setLatitude(40.71);
        profile.setLongitude(-74.00);
        profile.setCity("NYC");
        profile.setAvailability(AvailabilityStatus.AVAILABLE);
        profile.setNotificationPreferences(new NotificationPreferences(
                NotificationFrequency.IMMEDIATE, null, true, 25));
        profile.setPermanentlyRestricted(false);
        profile.setFlaggedForManualReview(false);
        profile.setReliabilityScore(80.0);
        profile.setProfileComplete(true);
        profile.setTotalDonations(5);
        profile.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        profile.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));
        return profile;
    }

    @Test
    void toProfileResponseMapsAllFields() {
        var profile = aProfile();
        var response = mapper.toProfileResponse(profile);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.bloodType()).isEqualTo(BloodType.A_POSITIVE);
        assertThat(response.bloodTypeVerified()).isTrue();
        assertThat(response.latitude()).isEqualTo(40.71);
        assertThat(response.longitude()).isEqualTo(-74.00);
        assertThat(response.city()).isEqualTo("NYC");
        assertThat(response.availability()).isEqualTo(AvailabilityStatus.AVAILABLE);
        assertThat(response.notificationPreferences()).isNotNull();
        assertThat(response.permanentlyRestricted()).isFalse();
        assertThat(response.flaggedForManualReview()).isFalse();
        assertThat(response.reliabilityScore()).isEqualTo(80.0);
        assertThat(response.profileComplete()).isTrue();
        assertThat(response.totalDonations()).isEqualTo(5);
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
        assertThat(response.updatedAt()).isEqualTo(Instant.parse("2025-06-01T00:00:00Z"));
    }

    @Test
    void toHealthResponseMapsAllFields() {
        var q = new HealthQuestionnaire(10L);
        q.setId(100L);
        q.setHasChronicIllness(true);
        q.setMedicalConditionsDetails("Diabetes");
        q.setOnMedication(true);
        q.setMedicationDetails("Insulin");
        q.setLastSurgeryAt(Instant.parse("2025-01-01T00:00:00Z"));
        q.setLastTravelAt(Instant.parse("2025-03-15T00:00:00Z"));
        q.setCreatedAt(Instant.parse("2025-03-01T00:00:00Z"));
        q.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));

        var response = mapper.toHealthResponse(q);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.hasChronicIllness()).isTrue();
        assertThat(response.medicalConditionsDetails()).isEqualTo("Diabetes");
        assertThat(response.onMedication()).isTrue();
        assertThat(response.medicationDetails()).isEqualTo("Insulin");
        assertThat(response.lastSurgeryAt()).isEqualTo(Instant.parse("2025-01-01T00:00:00Z"));
        assertThat(response.lastTravelAt()).isEqualTo(Instant.parse("2025-03-15T00:00:00Z"));
    }

    @Test
    void toEligibilityResponseReturnsEligibleWhenNoCooldown() {
        var profile = aProfile();
        var response = mapper.toEligibilityResponse(profile);

        assertThat(response.eligible()).isTrue();
    }

    @Test
    void toEligibilityResponseReturnsNotEligibleWhenRestricted() {
        var profile = aProfile();
        profile.setPermanentlyRestricted(true);
        profile.setRestrictionReason("Chronic illness");

        var response = mapper.toEligibilityResponse(profile);

        assertThat(response.eligible()).isFalse();
        assertThat(response.reason()).contains("Chronic illness");
    }
}
