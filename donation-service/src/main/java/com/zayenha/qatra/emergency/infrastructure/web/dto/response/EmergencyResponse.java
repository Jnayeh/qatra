package com.zayenha.qatra.emergency.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;

import java.time.Instant;

public record EmergencyResponse(
    Long id,
    String patientName,
    BloodType bloodType,
    Integer unitsNeeded,
    EmergencyUrgency urgency,
    String hospital,
    String hospitalAddress,
    Double latitude,
    Double longitude,
    String contactPhone,
    EmergencyStatus status,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt
) {}
