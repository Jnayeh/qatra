package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateFlagRequest(
    @NotNull boolean flaggedForManualReview
) {}
