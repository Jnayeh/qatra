package com.zayenha.qatra.center.infrastructure.web.dto.response;

import java.time.Instant;

public record CenterAdminDTO(
    Long id,
    Long userId,
    Long centerId,
    Instant createdAt
) {}
