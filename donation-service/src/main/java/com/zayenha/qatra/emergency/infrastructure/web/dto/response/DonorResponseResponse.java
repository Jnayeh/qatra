package com.zayenha.qatra.emergency.infrastructure.web.dto.response;

import com.zayenha.qatra.emergency.domain.model.ResponseStatus;

import java.time.Instant;

public record DonorResponseResponse(
    Long id,
    Long emergencyId,
    Long donorId,
    Long slotId,
    ResponseStatus status,
    Instant respondedAt,
    Instant createdAt
) {}
