package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityRequest(
    @NotNull AvailabilityStatus status
) {}
