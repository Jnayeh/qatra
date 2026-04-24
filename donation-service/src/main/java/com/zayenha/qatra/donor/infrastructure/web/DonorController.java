package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.*;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorDetailResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorHealthResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.DonorProfileResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityDetailResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.EligibilityResponse;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.ImpactResponse;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapper;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class DonorController {

    private final DonorCommandUseCases donorCommandUseCases;
    private final DonorQueryUseCases donorQueryUseCases;
    private final QuestionnaireCommandUseCases healthCommandUseCases;
    private final QuestionnaireQueryUseCases healthQueryUseCases;
    private final DonorMapper mapper;

    @GetMapping("/api/v1/donors/me/health-questionnaire")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> getHealthQuestionnaire() {
        var userId = AuditUtils.currentUserId();
        var questionnaire = healthQueryUseCases.getHealthQuestionnaire(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toHealthResponse(questionnaire)));
    }

    @PutMapping("/api/v1/donors/me/health-questionnaire")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> updateHealthQuestionnaire(
            @Valid @RequestBody HealthQuestionnaireRequest request) {
        var userId = AuditUtils.currentUserId();
        var command = new QuestionnaireCommandUseCases.HealthQuestionnaireCommand(
                request.hasChronicIllness(), request.medicalConditionsDetails(),
                request.onMedication(), request.medicationDetails(),
                request.lastSurgeryAt(), request.lastTravelAt(),
                request.lastTattooOrPiercingAt()
        );
        var questionnaire = healthCommandUseCases.updateHealthQuestionnaire(userId, command);
        return ResponseEntity.ok(ApiResponse.success(mapper.toHealthResponse(questionnaire)));
    }

    @GetMapping("/api/v1/donors/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getMyProfile() {
        var userId = AuditUtils.currentUserId();
        var profile = donorQueryUseCases.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateDonorRequest request) {
        var userId = AuditUtils.currentUserId();
        var command = new DonorCommandUseCases.UpdateProfileCommand(
                request.displayName(), request.phone());
        var profile = donorCommandUseCases.updateProfile(userId, command);
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/blood-type")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateBloodType(
            @Valid @RequestBody UpdateBloodTypeRequest request) {
        var userId = AuditUtils.currentUserId();
        var profile = donorCommandUseCases.updateBloodType(userId, request.bloodType());
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/location")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request) {
        var userId = AuditUtils.currentUserId();
        var command = new DonorCommandUseCases.UpdateLocationCommand(
                request.latitude(), request.longitude(),
                request.city(), request.country());
        var profile = donorCommandUseCases.updateLocation(userId, command);
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/availability")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateAvailability(
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        var userId = AuditUtils.currentUserId();
        var profile = donorCommandUseCases.updateAvailability(userId, request.status());
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PutMapping("/api/v1/donors/me/notification-prefs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateNotificationPrefs(
            @Valid @RequestBody UpdateNotificationPrefsRequest request) {
        var userId = AuditUtils.currentUserId();
        var profile = donorCommandUseCases.updateNotificationPrefs(userId, request.preferences());
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @GetMapping("/api/v1/donors/me/eligibility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<EligibilityResponse>> getEligibility() {
        var userId = AuditUtils.currentUserId();
        var profile = donorQueryUseCases.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toEligibilityResponse(profile)));
    }

    @GetMapping("/api/v1/donors/me/impact")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<ImpactResponse>> getImpact() {
        var userId = AuditUtils.currentUserId();
        var result = donorQueryUseCases.getImpact(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toImpactResponse(result)));
    }

    @GetMapping("/api/v1/donors/{id}/eligibility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<EligibilityDetailResponse>> getDonorEligibility(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toEligibilityDetailResponse(profile)));
    }

    @DeleteMapping("/api/v1/donors/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<String>> requestDeletion() {
        var userId = AuditUtils.currentUserId();
        donorCommandUseCases.requestDeletion(userId);
        return ResponseEntity.ok(ApiResponse.success("Deletion requested"));
    }

    @GetMapping("/api/v1/donors/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<DonorDetailResponse>> getDonorById(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toDetailResponse(profile)));
    }

    @PatchMapping("/api/v1/donors/{id}/restriction")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateRestriction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRestrictionRequest request) {
        var profile = donorCommandUseCases.updateRestriction(
                id, request.permanentlyRestricted(), request.restrictionReason());
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }

    @PatchMapping("/api/v1/donors/{id}/flag")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateFlag(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFlagRequest request) {
        var profile = donorCommandUseCases.updateFlag(id, request.flaggedForManualReview());
        return ResponseEntity.ok(ApiResponse.success(mapper.toProfileResponse(profile)));
    }
}
