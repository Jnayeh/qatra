package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record HealthQuestionnaireRequest(
    @NotNull Boolean hasChronicIllness,
    String medicalConditionsDetails,
    @NotNull Boolean onMedication,
    String medicationDetails,
    Instant lastSurgeryAt,
    Instant lastTravelAt,
    Instant lastTattooOrPiercingAt
) {}
