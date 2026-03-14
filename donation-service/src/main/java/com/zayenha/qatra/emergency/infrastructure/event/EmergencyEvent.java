package com.zayenha.qatra.emergency.infrastructure.event;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;

public record EmergencyEvent(
    Long emergencyId,
    BloodType bloodType,
    Integer unitsNeeded,
    EmergencyUrgency urgency,
    EmergencyStatus status,
    String action
) {}
