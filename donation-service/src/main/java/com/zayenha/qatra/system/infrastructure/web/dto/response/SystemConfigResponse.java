package com.zayenha.qatra.system.infrastructure.web.dto.response;

import java.time.Instant;

public record SystemConfigResponse(
    Long id,
    String key,
    String value,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
