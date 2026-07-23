package com.zayenha.qatra.donor.infrastructure.web;

import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.donor.application.PdfCertificateService;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapper;
import com.zayenha.qatra.donor.infrastructure.web.dto.request.*;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DonorController {

    private final DonorCommandUseCases donorCommandUseCases;
    private final DonorQueryUseCases donorQueryUseCases;
    private final QuestionnaireCommandUseCases healthCommandUseCases;
    private final QuestionnaireQueryUseCases healthQueryUseCases;
    private final DonorMapper mapper;
    private final PdfCertificateService pdfCertificateService;

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
    @PreAuthorize("hasAnyRole('DONOR')")
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
        var profile = donorCommandUseCases.updateProfile(userId);
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
    @PreAuthorize("hasAnyRole('DONOR')")
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

    @GetMapping("/api/v1/donors/me/certificates")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<java.util.List<CertificateResponse>>> getCertificates() {
        var userId = AuditUtils.currentUserId();
        var certs = donorQueryUseCases.getCertificates(userId).stream()
                .map(c -> new CertificateResponse(
                        c.id(), c.appointmentId(), c.donorName(), c.centerName(),
                        c.mlCollected(), c.donationDate(),
                        "/api/v1/donors/me/certificates/" + c.id() + "/download"))
                .toList();
        return ResponseEntity.ok(ApiResponse.success(certs));
    }

    @GetMapping("/api/v1/donors/me/certificates/{id}/download")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable Long id) {
        var pdfBytes = pdfCertificateService.generateCertificate(id);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/api/v1/donors/{id}/eligibility")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<EligibilityDetailResponse>> getDonorEligibility(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toEligibilityDetailResponse(profile)));
    }

    @GetMapping("/api/v1/donors/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<DonorDetailResponse>> getDonorById(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        var healthQuestionnaire = healthQueryUseCases.getHealthQuestionnaire(profile.getUserId());
        return ResponseEntity.ok(ApiResponse.success(mapper.toDetailResponse(profile, healthQuestionnaire)));
    }

    @GetMapping("/api/v1/donors/{id}/health-questionnaire")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<DonorHealthResponse>> getDonorHealthQuestionnaire(
            @PathVariable Long id) {
        var profile = donorQueryUseCases.getDonorById(id);
        var questionnaire = healthQueryUseCases.getHealthQuestionnaire(profile.getUserId());
        return ResponseEntity.ok(ApiResponse.success(mapper.toHealthResponse(questionnaire)));
    }

    @GetMapping("/api/v1/admin/donors/restricted")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<com.zayenha.qatra.user.infrastructure.web.dto.response.RestrictedUserResponse>>> getRestrictedDonors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = donorQueryUseCases.getPermanentlyRestrictedDonors(PageHelper.toPageIndex(page), size);
        return ResponseEntity.ok(ApiResponse.success(result.content(), PageHelper.fromDomain(result)));
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
