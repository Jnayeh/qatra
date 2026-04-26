package com.zayenha.qatra.emergency.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;

import java.time.Instant;

public record EmergencyResponse(
    Long id,
    Long centerId,
    BloodType bloodType,
    Integer unitsNeeded,
    EmergencyUrgency urgency,
    Integer matchRadius,
    Integer escalationLevel,
    String contactPhone,
    EmergencyStatus status,
    Instant createdAt,
    Instant updatedAt,
    Instant expiresAt,
    Instant resolvedAt,
    Long resolvedByUserId
) {}
