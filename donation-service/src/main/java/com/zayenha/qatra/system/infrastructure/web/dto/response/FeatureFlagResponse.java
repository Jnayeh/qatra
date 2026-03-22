package com.zayenha.qatra.system.infrastructure.web.dto.response;

import java.time.Instant;

public record FeatureFlagResponse(
    Long id,
    String flagName,
    boolean enabled,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
