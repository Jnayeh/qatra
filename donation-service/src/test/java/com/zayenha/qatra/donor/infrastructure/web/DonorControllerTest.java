package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra.donor.application.PdfCertificateService;
import com.zayenha.qatra.donor.domain.model.*;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapper;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.*;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.*;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.exception.GlobalExceptionHandler;
import com.zayenha.qatra._shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorControllerTest {

    @Mock private DonorCommandUseCases commandUseCases;
    @Mock private DonorQueryUseCases queryUseCases;
    @Mock private QuestionnaireCommandUseCases healthCommandUseCases;
    @Mock private QuestionnaireQueryUseCases healthQueryUseCases;
    @Mock private DonorMapper mapper;
    @Mock private PdfCertificateService pdfCertificateService;

    private DonorController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new DonorController(commandUseCases, queryUseCases, healthCommandUseCases, healthQueryUseCases, mapper, pdfCertificateService);
        exceptionHandler = new GlobalExceptionHandler();
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(1L, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);
        profile.setAvailability(AvailabilityStatus.AVAILABLE);
        profile.setNotificationPreferences(new NotificationPreferences(
                NotificationFrequency.IMMEDIATE, null, true, 25));
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        return profile;
    }

    @Test
    void getMyProfileReturnsProfile() {
        var profile = aProfile();
        when(queryUseCases.getMyProfile(1L)).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var response = controller.getMyProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().userId()).isEqualTo(1L);
        assertThat(response.getBody().data().bloodType()).isEqualTo(BloodType.A_POSITIVE);
    }

    @Test
    void getMyProfileCreatesNewWhenNotFound() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        when(queryUseCases.getMyProfile(1L)).thenReturn(profile);

        var response = controller.getMyProfile();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateBloodTypeReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateBloodType(eq(1L), any())).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var request = new UpdateBloodTypeRequest(BloodType.A_POSITIVE);
        var response = controller.updateBloodType(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().bloodType()).isEqualTo(BloodType.A_POSITIVE);
    }

    @Test
    void updateLocationReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateLocation(eq(1L), any())).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var request = new UpdateLocationRequest(40.71, -74.00, "NYC", "USA");
        var response = controller.updateLocation(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateAvailabilityReturnsProfile() {
        var profile = aProfile();
        profile.setAvailability(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
        when(commandUseCases.updateAvailability(eq(1L), any())).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var request = new UpdateAvailabilityRequest(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
        var response = controller.updateAvailability(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().availability())
                .isEqualTo(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
    }

    @Test
    void getHealthQuestionnaireReturnsQuestionnaire() {
        var q = new HealthQuestionnaire(10L);
        q.setId(100L);
        q.setHasChronicIllness(false);
        q.setOnMedication(false);
        q.setCreatedAt(Instant.now());
        q.setUpdatedAt(Instant.now());
        when(healthQueryUseCases.getHealthQuestionnaire(1L)).thenReturn(q);
        when(mapper.toHealthResponse(q)).thenReturn(new DonorHealthResponse(
                q.getId(), q.getDonorId(), q.getHasChronicIllness(),
                q.getMedicalConditionsDetails(), q.getOnMedication(),
                q.getMedicationDetails(), q.getLastSurgeryAt(), q.getLastTravelAt(),
                q.getLastTattooOrPiercingAt(), q.getCreatedAt(), q.getUpdatedAt()));

        var response = controller.getHealthQuestionnaire();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(100L);
    }

    @Test
    void updateHealthQuestionnaireReturnsQuestionnaire() {
        var q = new HealthQuestionnaire(10L);
        q.setId(100L);
        q.setHasChronicIllness(false);
        q.setOnMedication(false);
        q.setCreatedAt(Instant.now());
        q.setUpdatedAt(Instant.now());
        when(healthCommandUseCases.updateHealthQuestionnaire(eq(1L), any())).thenReturn(q);
        when(mapper.toHealthResponse(q)).thenReturn(new DonorHealthResponse(
                q.getId(), q.getDonorId(), q.getHasChronicIllness(),
                q.getMedicalConditionsDetails(), q.getOnMedication(),
                q.getMedicationDetails(), q.getLastSurgeryAt(), q.getLastTravelAt(),
                q.getLastTattooOrPiercingAt(), q.getCreatedAt(), q.getUpdatedAt()));

        var request = new HealthQuestionnaireRequest(
                false, null, false, null, null, null, null);
        var response = controller.updateHealthQuestionnaire(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().hasChronicIllness()).isFalse();
    }

    @Test
    void getEligibilityReturnsEligibility() {
        var profile = aProfile();
        when(queryUseCases.getMyProfile(1L)).thenReturn(profile);
        when(mapper.toEligibilityResponse(profile)).thenReturn(new EligibilityResponse(
                true, profile.getEligibleFromDate(), null));

        var response = controller.getEligibility();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().eligible()).isTrue();
    }

    @Test
    void getImpactReturnsImpactResponse() {
        var impact = new DonorQueryUseCases.ImpactResult(5, 15, List.of("First donation completed"));
        when(queryUseCases.getImpact(1L)).thenReturn(impact);
        when(mapper.toImpactResponse(impact)).thenReturn(new ImpactResponse(
                impact.totalDonations(), impact.estimatedLivesSaved(), impact.milestones()));

        var response = controller.getImpact();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().totalDonations()).isEqualTo(5);
        assertThat(response.getBody().data().estimatedLivesSaved()).isEqualTo(15);
        assertThat(response.getBody().data().milestones()).hasSize(1);
    }

    @Test
    void getDonorEligibilityReturnsEligibilityDetail() {
        var profile = aProfile();
        when(queryUseCases.getDonorById(10L)).thenReturn(profile);
        when(mapper.toEligibilityDetailResponse(profile)).thenReturn(new EligibilityDetailResponse(
                true, profile.getEligibleFromDate(),
                Boolean.TRUE.equals(profile.getPermanentlyRestricted()),
                profile.getRestrictionReason()));

        var response = controller.getDonorEligibility(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().eligible()).isTrue();
    }

    @Test
    void getDonorByIdReturnsDetail() {
        var profile = aProfile();
        when(queryUseCases.getDonorById(10L)).thenReturn(profile);
        when(mapper.toDetailResponse(profile)).thenReturn(new DonorDetailResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), null, 0,
                profile.getCreatedAt(), profile.getUpdatedAt()));

        var response = controller.getDonorById(10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().userId()).isEqualTo(1L);
    }

    @Test
    void updateRestrictionReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateRestriction(eq(10L), anyBoolean(), any())).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var request = new UpdateRestrictionRequest(true, "Cheating");
        var response = controller.updateRestriction(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateFlagReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateFlag(eq(10L), anyBoolean())).thenReturn(profile);
        when(mapper.toProfileResponse(profile)).thenReturn(new DonorProfileResponse(
                profile.getId(), profile.getUserId(), profile.getBloodType(),
                profile.getBloodTypeVerified(), profile.getLatitude(), profile.getLongitude(),
                profile.getCity(), profile.getAvailability(), profile.getNotificationPreferences(),
                profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
                profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
                profile.getEligibleFromDate(), profile.getProfileComplete(),
                profile.getTotalDonations(), profile.getCreatedAt(), profile.getUpdatedAt()));

        var request = new UpdateFlagRequest(true);
        var response = controller.updateFlag(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    // --- ExceptionHandler tests ---

    @Test
    void notFoundReturns404() {
        var ex = new NotFoundException("Donor not found: 99", "DONOR_NOT_FOUND");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("99");
    }

    @Test
    void conflictReturns409() {
        var ex = new com.zayenha.qatra._shared.exception.ConflictException(
                "Blood type already verified", "BLOOD_TYPE_ALREADY_VERIFIED");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("already verified");
    }
}
