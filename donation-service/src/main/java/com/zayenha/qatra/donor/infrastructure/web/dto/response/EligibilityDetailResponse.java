package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.Instant;

public record EligibilityDetailResponse(
    boolean eligible,
    Instant nextEligibleDate,
    boolean permanentlyRestricted,
    String restrictionReason
) {}
