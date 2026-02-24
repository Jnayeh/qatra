package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.Instant;

public record EligibilityResponse(
    boolean eligible,
    Instant nextEligibleDate,
    String reason
) {}
