package com.zayenha.qatra.emergency.infrastructure.web.dto.request;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmergencyRequest(
    @NotNull Long centerId,
    @NotNull BloodType bloodType,
    @NotNull Integer unitsNeeded,
    @NotNull EmergencyUrgency urgency,
    @NotNull Integer matchRadius,
    String contactPhone
) {}
