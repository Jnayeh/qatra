package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateRestrictionRequest(
    @NotNull boolean permanentlyRestricted,
    String restrictionReason
) {}
