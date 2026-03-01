package com.zayenha.qatra.center.infrastructure.web.dto.response;

import java.time.Instant;

public record StaffSummaryResponse(
    Long id,
    Long userId,
    Long centerId,
    boolean verified,
    Instant createdAt
) {}
