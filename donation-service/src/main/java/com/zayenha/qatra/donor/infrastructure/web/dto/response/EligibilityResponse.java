package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.LocalDate;

public record EligibilityResponse(
    boolean eligible,
    LocalDate eligibleFromDate,
    String reason
) {}
