package com.zayenha.qatra.donor.application;

import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireQueryUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireService implements QuestionnaireCommandUseCases, QuestionnaireQueryUseCases {

    private final DonorRepositoryPort donorRepository;

    @Override
    @Transactional
    public HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Donor not found: " + userId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));

        var questionnaire = donorRepository.findQuestionnaireByDonorId(profile.getId())
                .orElseGet(() -> new HealthQuestionnaire(profile.getId()));

        questionnaire.setHasChronicIllness(command.hasChronicIllness());
        questionnaire.setMedicalConditionsDetails(command.medicalConditionsDetails());
        questionnaire.setOnMedication(command.onMedication());
        questionnaire.setMedicationDetails(command.medicationDetails());
        questionnaire.setRecentSurgery(command.recentSurgery());
        questionnaire.setRecentTravel(command.recentTravel());
        questionnaire.setRecentTattooOrPiercing(command.recentTattooOrPiercing());
        questionnaire.setUpdatedAt(Instant.now());

        evaluatePermanentRestriction(profile, command);
        profile.setUpdatedAt(Instant.now());
        boolean hasLocation = profile.getLatitude() != null && profile.getLongitude() != null;
        profile.setProfileComplete(hasLocation);

        donorRepository.save(profile); 
        return donorRepository.saveQuestionnaire(questionnaire);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthQuestionnaire getHealthQuestionnaire(Long userId) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Donor not found: " + userId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        return donorRepository.findQuestionnaireByDonorId(profile.getId())
                .orElseThrow(() -> new NotFoundException("Health questionnaire not found",
                        DonorErrorCode.HEALTH_QUESTIONNAIRE_NOT_FOUND.name()));
    }

    private void evaluatePermanentRestriction(DonorProfile profile, HealthQuestionnaireCommand command) {
        if (command.hasChronicIllness()) {
            profile.setRestrictionReason("Chronic illness indicated in health questionnaire");
        } else if (command.onMedication() && command.medicationDetails() != null
                && containsPermanentMedicationKeyword(command.medicationDetails())) {
            profile.setRestrictionReason("Permanent medication: " + command.medicationDetails());
        }
    }

    private boolean containsPermanentMedicationKeyword(String details) {
        var keywords = List.of("insulin", "chemo", "immunosuppressant");
        var lower = details.toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }

    private boolean hasQuestionnaire(Long id, HealthQuestionnaire questionnaire) {
        if (questionnaire != null) return true;
        return donorRepository.donorHasQuestionnaire(id);
    }
}
