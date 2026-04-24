package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.Instant;

public record DonorHealthResponse(
        Long id,
        Long donorId,
        Boolean hasChronicIllness,
        String medicalConditionsDetails,
        Boolean onMedication,
        String medicationDetails,
        Instant lastSurgeryAt,
        Instant lastTravelAt,
        Instant lastTattooOrPiercingAt,
        Instant createdAt,
        Instant updatedAt
) {}
