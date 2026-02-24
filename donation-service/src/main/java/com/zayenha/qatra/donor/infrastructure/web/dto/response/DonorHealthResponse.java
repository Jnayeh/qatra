package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.Instant;

public record DonorHealthResponse(
        Long id,
        Long donorId,
        boolean hasChronicIllness,
        String medicalConditionsDetails,
        boolean onMedication,
        String medicationDetails,
        boolean recentSurgery,
        boolean recentTravel,
        boolean recentTattooOrPiercing,
        Instant createdAt,
        Instant updatedAt
) {}
