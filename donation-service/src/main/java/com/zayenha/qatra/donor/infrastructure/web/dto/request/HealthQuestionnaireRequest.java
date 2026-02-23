package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record HealthQuestionnaireRequest(
    @NotNull boolean hasChronicIllness,
    String medicalConditionsDetails,
    @NotNull boolean onMedication,
    String medicationDetails,
    @NotNull boolean recentSurgery,
    @NotNull boolean recentTravel,
    @NotNull boolean recentTattooOrPiercing
) {}
