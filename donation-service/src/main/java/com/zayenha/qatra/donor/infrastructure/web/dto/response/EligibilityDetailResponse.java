package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.LocalDate;

public record EligibilityDetailResponse(
    boolean eligible,
    LocalDate eligibleFromDate,
    boolean permanentlyRestricted,
    String reason
) {}
