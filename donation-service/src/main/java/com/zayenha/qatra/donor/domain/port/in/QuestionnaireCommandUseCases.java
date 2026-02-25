package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

public interface QuestionnaireCommandUseCases {
    HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command);

    record HealthQuestionnaireCommand(
        boolean hasChronicIllness,
        String medicalConditionsDetails,
        boolean onMedication,
        String medicationDetails,
        boolean recentSurgery,
        boolean recentTravel,
        boolean recentTattooOrPiercing
    ) {}
}
