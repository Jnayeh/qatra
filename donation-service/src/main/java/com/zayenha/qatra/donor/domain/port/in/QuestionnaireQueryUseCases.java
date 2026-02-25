package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

public interface QuestionnaireQueryUseCases {
    HealthQuestionnaire getHealthQuestionnaire(Long userId);
}
