package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.domain.BloodType;
import jakarta.validation.constraints.NotNull;

public record UpdateBloodTypeRequest(
    @NotNull BloodType bloodType
) {}
