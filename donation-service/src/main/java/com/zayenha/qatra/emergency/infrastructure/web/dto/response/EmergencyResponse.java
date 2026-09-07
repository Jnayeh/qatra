package com.zayenha.qatra.emergency.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.enums.BloodType;
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
    Long resolvedByUserId,
    long matchedDonorCount,
    long pendingMatchCount,
    long responseCount,
    long acceptedCount,
    long declinedCount
) {

    public EmergencyResponse withStats(long matchedDonorCount, long pendingMatchCount,
                                       long responseCount, long acceptedCount, long declinedCount) {
        return new EmergencyResponse(
            id, centerId, bloodType, unitsNeeded, urgency, matchRadius, escalationLevel,
            contactPhone, status, createdAt, updatedAt, expiresAt, resolvedAt, resolvedByUserId,
            matchedDonorCount, pendingMatchCount, responseCount, acceptedCount, declinedCount);
    }
}
