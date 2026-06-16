package com.zayenha.qatra.donor.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.NotificationFrequency;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonationCertificateJpaRepository;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepositoryPort donorRepository;
    @Mock
    private CacheService cacheService;
    @Mock
    private AuditPublisher auditPublisher;
    @Mock
    private DonationCertificateJpaRepository certificateRepository;
    @Mock
    private UserApi userApi;

    private DonorService donorService;

    @BeforeEach
    void setUp() {
        donorService = new DonorService(donorRepository, certificateRepository, cacheService, auditPublisher, userApi);
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        return profile;
    }



    @Test
    void getMyProfileReturnsExistingProfile() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        var result = donorService.getMyProfile(1L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getMyProfileThrowsWhenNotFound() {
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> donorService.getMyProfile(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("1");
    }



    @Test
    void updateBloodTypeSetsBloodType() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodType(1L, BloodType.A_POSITIVE);

        assertThat(result.getBloodType()).isEqualTo(BloodType.A_POSITIVE);
    }

    @Test
    void updateBloodTypeDoesNotSetVerifiedForUnknown() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodType(1L, BloodType.UNKNOWN);

        assertThat(result.getBloodType()).isEqualTo(BloodType.UNKNOWN);
        assertThat(result.getBloodTypeVerified()).isFalse();
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



    @Test
    void updateBloodTypeAdminOverridesVerification() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateBloodTypeAdmin(10L, BloodType.O_NEGATIVE);

        assertThat(result.getBloodType()).isEqualTo(BloodType.O_NEGATIVE);
        assertThat(result.getBloodTypeVerified()).isTrue();
    }



    @Test
    void updateLocationSetsCoordinates() {
        var profile = aProfile();
        profile.setLatitude(0.0);
        profile.setLongitude(0.0);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var command = new DonorCommandUseCases.UpdateLocationCommand(40.71, -74.00, "NYC", "USA");
        var result = donorService.updateLocation(1L, command);

        assertThat(result.getLatitude()).isEqualTo(40.71);
        assertThat(result.getLongitude()).isEqualTo(-74.00);
        assertThat(result.getCity()).isEqualTo("NYC");
    }



    @Test
    void updateAvailabilityChangesStatus() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateAvailability(1L, AvailabilityStatus.TEMPORARILY_UNAVAILABLE);

        assertThat(result.getAvailability()).isEqualTo(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
    }



    @Test
    void requestDeletionSetsStatusToInactive() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        donorService.requestDeletion(1L);

        assertThat(profile.getStatus()).isEqualTo(DonorStatus.PENDING_DELETION);
    }



    @Test
    void updateRestrictionChangesRestriction() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateRestriction(10L, true, "Manual override");

        assertThat(result.getPermanentlyRestricted()).isTrue();
        assertThat(result.getRestrictionReason()).isEqualTo("Manual override");
    }



    @Test
    void updateFlagChangesFlag() {
        var profile = aProfile();
        when(donorRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = donorService.updateFlag(10L, true);

        assertThat(result.getFlaggedForManualReview()).isTrue();
    }



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



    @Test
    void getImpactReturnsImpactResult() {
        var profile = aProfile();
        profile.setTotalDonations(3);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));

        var result = donorService.getImpact(1L);

        assertThat(result.totalDonations()).isEqualTo(3);
        assertThat(result.milestones()).contains("First donation completed");
    }



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
