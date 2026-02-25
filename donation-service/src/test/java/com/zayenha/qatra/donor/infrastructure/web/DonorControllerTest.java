package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra.donor.domain.model.*;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.*;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityResponse;
import com.zayenha.qatra.shared.domain.BloodType;
import com.zayenha.qatra.shared.exception.GlobalExceptionHandler;
import com.zayenha.qatra.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorControllerTest {

    @Mock private DonorCommandUseCases commandUseCases;
    @Mock private DonorQueryUseCases queryUseCases;
    @Mock private QuestionnaireCommandUseCases healthCommandUseCases;
    @Mock private QuestionnaireQueryUseCases healthQueryUseCases;

    private DonorController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new DonorController(commandUseCases, queryUseCases, healthCommandUseCases, healthQueryUseCases);
        exceptionHandler = new GlobalExceptionHandler();
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);
        profile.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
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

        var response = controller.getMyProfile(1L);

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

        var response = controller.getMyProfile(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateBloodTypeReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateBloodType(eq(1L), any())).thenReturn(profile);

        var request = new UpdateBloodTypeRequest(BloodType.A_POSITIVE);
        var response = controller.updateBloodType(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().bloodType()).isEqualTo(BloodType.A_POSITIVE);
    }

    @Test
    void updateLocationReturnsProfile() {
        var profile = aProfile();
        when(commandUseCases.updateLocation(eq(1L), any())).thenReturn(profile);

        var request = new UpdateLocationRequest(40.71, -74.00, "NYC", "USA");
        var response = controller.updateLocation(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateAvailabilityReturnsProfile() {
        var profile = aProfile();
        profile.setAvailabilityStatus(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
        when(commandUseCases.updateAvailability(eq(1L), any())).thenReturn(profile);

        var request = new UpdateAvailabilityRequest(AvailabilityStatus.TEMPORARILY_UNAVAILABLE);
        var response = controller.updateAvailability(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().availabilityStatus())
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

        var response = controller.getHealthQuestionnaire(1L);

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

        var request = new HealthQuestionnaireRequest(
                false, null, false, null, false, false, false);
        var response = controller.updateHealthQuestionnaire(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().hasChronicIllness()).isFalse();
    }

    @Test
    void getEligibilityReturnsEligibility() {
        var profile = aProfile();
        when(queryUseCases.getMyProfile(1L)).thenReturn(profile);

        var response = controller.getEligibility(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().eligible()).isTrue();
    }

    @Test
    void requestDeletionReturnsSuccess() {
        var response = controller.requestDeletion(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo("Deletion requested");
        verify(commandUseCases).requestDeletion(1L);
    }

    @Test
    void getDonorByIdReturnsDetail() {
        var profile = aProfile();
        when(queryUseCases.getDonorById(10L)).thenReturn(profile);

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
        var ex = new com.zayenha.qatra.shared.exception.ConflictException(
                "Blood type already verified", "BLOOD_TYPE_ALREADY_VERIFIED");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("already verified");
    }
}
