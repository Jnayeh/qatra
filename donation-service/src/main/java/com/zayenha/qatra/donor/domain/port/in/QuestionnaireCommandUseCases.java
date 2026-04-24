package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

import java.time.Instant;

public interface QuestionnaireCommandUseCases {
    HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command);

    record HealthQuestionnaireCommand(
        Boolean hasChronicIllness,
        String medicalConditionsDetails,
        Boolean onMedication,
        String medicationDetails,
        Instant lastSurgeryAt,
        Instant lastTravelAt,
        Instant lastTattooOrPiercingAt
    ) {}
}
