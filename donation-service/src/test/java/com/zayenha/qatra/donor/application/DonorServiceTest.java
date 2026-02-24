package com.zayenha.qatra.donor.application;

import com.zayenha.qatra.donor.domain.model.*;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.shared.domain.BloodType;
import com.zayenha.qatra.shared.exception.ConflictException;
import com.zayenha.qatra.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepositoryPort donorRepository;

    private DonorService donorService;

    @BeforeEach
    void setUp() {
        donorService = new DonorService(donorRepository);
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        return profile;
    }

    private HealthQuestionnaire aQuestionnaire(Long donorId) {
        var q = new HealthQuestionnaire(donorId);
        q.setId(100L);
        return q;
    }

    // --- getMyProfile ---

    @Test
    void getMyProfileReturnsExistingProfile() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        var result = donorService.getMyProfile(1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getMyProfileCreatesNewWhenNotFound() {
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(donorRepository.save(any())).thenAnswer(invocation -> {
            var p = invocation.<DonorProfile>getArgument(0);
            p.setId(10L);
            return p;
        });

        var result = donorService.getMyProfile(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getBloodType()).isEqualTo(BloodType.UNKNOWN);
        assertThat(result.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.AVAILABLE);
    }

    // --- updateBloodType ---

    @Test
    void updateBloodTypeSetsVerifiedWhenNotUnknown() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodType(1L, BloodType.A_POSITIVE);

        assertThat(result.getBloodType()).isEqualTo(BloodType.A_POSITIVE);
        assertThat(result.isBloodTypeVerified()).isTrue();
    }

    @Test
    void updateBloodTypeDoesNotSetVerifiedForUnknown() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodType(1L, BloodType.UNKNOWN);

        assertThat(result.getBloodType()).isEqualTo(BloodType.UNKNOWN);
        assertThat(result.isBloodTypeVerified()).isFalse();
    }

    @Test
    void updateBloodTypeThrowsWhenAlreadyVerified() {
        var profile = aProfile();
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> donorService.updateBloodType(1L, BloodType.B_POSITIVE))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already verified");
    }

    // --- updateBloodTypeAdmin ---

    @Test
    void updateBloodTypeAdminOverridesVerification() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodTypeAdmin(10L, BloodType.O_NEGATIVE);

        assertThat(result.getBloodType()).isEqualTo(BloodType.O_NEGATIVE);
        assertThat(result.isBloodTypeVerified()).isTrue();
    }

    // --- updateLocation ---

    @Test
    void updateLocationSetsCoordinates() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var command = new DonorCommandUseCases.UpdateLocationCommand(40.71, -74.00, "NYC", "USA");
        var result = donorService.updateLocation(1L, command);

        assertThat(result.getLatitude()).isEqualTo(40.71);
        assertThat(result.getLongitude()).isEqualTo(-74.00);
        assertThat(result.getCity()).isEqualTo("NYC");
        assertThat(result.getCountry()).isEqualTo("USA");
    }

    // --- updateAvailability ---

    @Test
    void updateAvailabilityChangesStatus() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateAvailability(1L, AvailabilityStatus.TEMPORARILY_UNAVAILABLE);

        assertThat(result.getAvailabilityStatus()).isEqualTo(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
    }

    // --- updateHealthQuestionnaire ---

    @Test
    void updateHealthQuestionnaireSetsPermanentRestrictionForChronicIllness() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(donorRepository.saveQuestionnaire(any())).thenAnswer(i -> i.getArgument(0));

        var command = new DonorCommandUseCases.HealthQuestionnaireCommand(
                true, null, false, null, false, false, false);
        var result = donorService.updateHealthQuestionnaire(1L, command);

        assertThat(result.isHasChronicIllness()).isTrue();
        assertThat(profile.isPermanentlyRestricted()).isTrue();
        assertThat(profile.getRestrictionReason()).contains("Chronic illness");
    }

    @Test
    void updateHealthQuestionnaireSetsPermanentRestrictionForMedicationKeyword() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(donorRepository.saveQuestionnaire(any())).thenAnswer(i -> i.getArgument(0));

        var command = new DonorCommandUseCases.HealthQuestionnaireCommand(
                false, null, true, "Patient takes insulin daily",
                false, false, false);
        var result = donorService.updateHealthQuestionnaire(1L, command);

        assertThat(result.isOnMedication()).isTrue();
        assertThat(profile.isPermanentlyRestricted()).isTrue();
        assertThat(profile.getRestrictionReason()).contains("insulin");
    }

    // --- requestDeletion ---

    @Test
    void requestDeletionSetsStatusToInactive() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        donorService.requestDeletion(1L);

        assertThat(profile.getStatus()).isEqualTo(DonorStatus.INACTIVE);
    }

    // --- updateRestriction ---

    @Test
    void updateRestrictionChangesRestriction() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateRestriction(10L, true, "Manual override");

        assertThat(result.isPermanentlyRestricted()).isTrue();
        assertThat(result.getRestrictionReason()).isEqualTo("Manual override");
    }

    // --- updateFlag ---

    @Test
    void updateFlagChangesFlag() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateFlag(10L, true);

        assertThat(result.isFlaggedForManualReview()).isTrue();
    }

    // --- getDonorById ---

    @Test
    void getDonorByIdReturnsProfile() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));

        var result = donorService.getDonorById(10L);

        assertThat(result).isEqualTo(profile);
    }

    @Test
    void getDonorByIdThrowsWhenNotFound() {
        when(donorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.getDonorById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // --- getHealthQuestionnaire ---

    @Test
    void getHealthQuestionnaireReturnsQuestionnaire() {
        var profile = aProfile();
        var q = aQuestionnaire(10L);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.of(q));

        var result = donorService.getHealthQuestionnaire(1L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getHealthQuestionnaireThrowsWhenNotFound() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.getHealthQuestionnaire(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Health questionnaire");
    }

    // --- updateNotificationPrefs ---

    @Test
    void updateNotificationPrefsSavesPreferences() {
        var profile = aProfile();
        var prefs = new NotificationPreferences(NotificationFrequency.DAILY_DIGEST, null, false, 10);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateNotificationPrefs(1L, prefs);

        assertThat(result.getNotificationPreferences().frequency()).isEqualTo(NotificationFrequency.DAILY_DIGEST);
        assertThat(result.getNotificationPreferences().allowEmergencyNotifications()).isFalse();
        assertThat(result.getNotificationPreferences().maxNotificationDistanceKm()).isEqualTo(10);
    }
}
