package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.*;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorDetailResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorProfileResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityResponse;
import com.zayenha.qatra.donor.infrastructure.web.mapper.DonorMapper;
import com.zayenha.qatra._shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DonorController {

    private final DonorCommandUseCases donorCommandUseCases;
    private final DonorQueryUseCases donorQueryUseCases;
    private final QuestionnaireCommandUseCases healthCommandUseCases;
    private final QuestionnaireQueryUseCases healthQueryUseCases;

    @GetMapping("/api/v1/donors/me/health-questionnaire")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> getHealthQuestionnaire(
            @RequestHeader("x-user-id") Long userId) {
        var questionnaire = healthQueryUseCases.getHealthQuestionnaire(userId);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toHealthResponse(questionnaire)));
    }

    @PutMapping("/api/v1/donors/me/health-questionnaire")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> updateHealthQuestionnaire(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody HealthQuestionnaireRequest request) {
        var command = new QuestionnaireCommandUseCases.HealthQuestionnaireCommand(
                request.hasChronicIllness(), request.medicalConditionsDetails(),
                request.onMedication(), request.medicationDetails(),
                request.recentSurgery(), request.recentTravel(),
                request.recentTattooOrPiercing()
        );
        var questionnaire = healthCommandUseCases.updateHealthQuestionnaire(userId, command);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toHealthResponse(questionnaire)));
    }

    @GetMapping("/api/v1/donors/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getMyProfile(
            @RequestHeader("x-user-id") Long userId) {
        var profile = donorQueryUseCases.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateProfile(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody UpdateDonorRequest request) {
        var command = new DonorCommandUseCases.UpdateProfileCommand(
                request.displayName(), request.phone());
        var profile = donorCommandUseCases.updateProfile(userId, command);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/blood-type")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateBloodType(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody UpdateBloodTypeRequest request) {
        var profile = donorCommandUseCases.updateBloodType(userId, request.bloodType());
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/location")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateLocation(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody UpdateLocationRequest request) {
        var command = new DonorCommandUseCases.UpdateLocationCommand(
                request.latitude(), request.longitude(),
                request.city(), request.country());
        var profile = donorCommandUseCases.updateLocation(userId, command);
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/availability")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateAvailability(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        var profile = donorCommandUseCases.updateAvailability(userId, request.status());
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/notification-prefs")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateNotificationPrefs(
            @RequestHeader("x-user-id") Long userId,
            @Valid @RequestBody UpdateNotificationPrefsRequest request) {
        var profile = donorCommandUseCases.updateNotificationPrefs(userId, request.preferences());
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @GetMapping("/api/v1/donors/me/eligibility")
    public ResponseEntity<ApiResponse<EligibilityResponse>> getEligibility(
            @RequestHeader("x-user-id") Long userId) {
        var profile = donorQueryUseCases.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(
                DonorMapper.toEligibilityResponse(profile)));
    }

    @DeleteMapping("/api/v1/donors/me")
    public ResponseEntity<ApiResponse<String>> requestDeletion(
            @RequestHeader("x-user-id") Long userId) {
        donorCommandUseCases.requestDeletion(userId);
        return ResponseEntity.ok(ApiResponse.success("Deletion requested"));
    }

    @GetMapping("/api/v1/donors/{id}")
    public ResponseEntity<ApiResponse<DonorDetailResponse>> getDonorById(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        var detail = new DonorDetailResponse(
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
            null, 0,
            profile.getCreatedAt(), profile.getUpdatedAt()
        );
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @PatchMapping("/api/v1/donors/{id}/restriction")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateRestriction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestrictionRequest request) {
        var profile = donorCommandUseCases.updateRestriction(
                id, request.permanentlyRestricted(), request.restrictionReason());
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }

    @PatchMapping("/api/v1/donors/{id}/flag")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateFlag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFlagRequest request) {
        var profile = donorCommandUseCases.updateFlag(id, request.flaggedForManualReview());
        return ResponseEntity.ok(ApiResponse.success(DonorMapper.toProfileResponse(profile)));
    }
}
