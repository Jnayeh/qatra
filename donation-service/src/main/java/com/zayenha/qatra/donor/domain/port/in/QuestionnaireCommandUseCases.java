package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public interface QuestionnaireCommandUseCases {
    HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command);

    record HealthQuestionnaireCommand(
        @NotNull Boolean hasChronicIllness,
        String medicalConditionsDetails,
        @NotNull Boolean onMedication,
        String medicationDetails,
        Instant lastSurgeryAt,
        Instant lastTravelAt,
        Instant lastTattooOrPiercingAt
    ) {}
}
