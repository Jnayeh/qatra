package com.zayenha.qatra.donor.application;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QuestionnaireService implements QuestionnaireCommandUseCases, QuestionnaireQueryUseCases {

    private final DonorRepositoryPort donorRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditPublisher auditPublisher;
    private final EventPublisherPort eventPublisherPort;
    private final UserApi userApi;

    @Value("${questionnaire.permanent-restriction-keywords:insulin,chemo,immunosuppressant}")
    private String restrictionKeywords;

    @Override
    @Transactional
    public HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Donor not found by userID: " + userId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));

        var questionnaire = donorRepository.findQuestionnaireByDonorId(profile.getId())
                .orElseGet(() -> new HealthQuestionnaire(profile.getId()));

        questionnaire.setHasChronicIllness(command.hasChronicIllness());
        questionnaire.setMedicalConditionsDetails(command.medicalConditionsDetails());
        questionnaire.setOnMedication(command.onMedication());
        questionnaire.setMedicationDetails(command.medicationDetails());
        questionnaire.setLastSurgeryAt(command.lastSurgeryAt());
        questionnaire.setLastTravelAt(command.lastTravelAt());
        questionnaire.setLastTattooOrPiercingAt(command.lastTattooOrPiercingAt());
        questionnaire.setUpdatedAt(Instant.now());

        evaluatePermanentRestriction(profile, command);
        profile.setUpdatedAt(Instant.now());
        boolean hasLocation = profile.getLatitude() != null && profile.getLongitude() != null;
        boolean complete = hasLocation;
        profile.setProfileComplete(complete);
        if (complete && profile.getStatus() == DonorStatus.INACTIVE) {
            profile.setStatus(DonorStatus.ACTIVE);
        }

        donorRepository.save(profile);
        var saved = donorRepository.saveQuestionnaire(questionnaire);

        auditPublisher.publish("HEALTH_QUESTIONNAIRE_UPDATED", saved.getId(), "HealthQuestionnaire", null,
            Map.of("userId", userId, "donorId", profile.getId(),
                   "hasChronicIllness", command.hasChronicIllness(),
                   "onMedication", command.onMedication()));
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public HealthQuestionnaire getHealthQuestionnaire(Long userId) {
        return donorRepository.findQuestionnaireByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Health questionnaire not found",
                        DonorErrorCode.HEALTH_QUESTIONNAIRE_NOT_FOUND.name()));
    }

    private void evaluatePermanentRestriction(DonorProfile profile, HealthQuestionnaireCommand command) {
        boolean restricted = false;
        if (command.hasChronicIllness()) {
            profile.setRestrictionReason("Chronic illness indicated in health questionnaire");
            restricted = true;
        } else if (command.onMedication() && command.medicationDetails() != null
                && containsPermanentMedicationKeyword(command.medicationDetails())) {
            profile.setRestrictionReason("Permanent medication: " + command.medicationDetails());
            restricted = true;
        }
        if (restricted) {
            profile.setPermanentlyRestricted(true);
            profile.setAvailability(AvailabilityStatus.PERMANENTLY_RESTRICTED);
            profile.setFlaggedForManualReview(true);
        }
    }


    private boolean containsPermanentMedicationKeyword(String details) {
        if (details == null) return false;
        var src = restrictionKeywords != null ? restrictionKeywords : "insulin,chemo,immunosuppressant";
        var lower = details.toLowerCase();
        for (var kw : src.split(",")) {
            if (lower.contains(kw.trim().toLowerCase())) return true;
        }
        return false;
    }

    private boolean hasQuestionnaire(Long id, HealthQuestionnaire questionnaire) {
        if (questionnaire != null) return true;
        return donorRepository.donorHasQuestionnaire(id);
    }
}
