package com.zayenha.qatra.system.infrastructure.web.dto.response;

import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;

import java.time.Instant;

public record GDPRDeletionResponse(
    Long id,
    Long userId,
    String reason,
    GDPRDeletionStatus status,
    Instant requestedAt,
    Instant processedAt,
    String processedBy
) {}
