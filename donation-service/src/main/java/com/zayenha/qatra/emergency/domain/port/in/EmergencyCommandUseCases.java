package com.zayenha.qatra.emergency.domain.port.in;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyUrgency;

public interface EmergencyCommandUseCases {
    EmergencyRequest create(Long centerId, Long createdByStaffId, BloodType bloodType, Integer unitsNeeded,
                            EmergencyUrgency urgency, Integer matchRadius, String contactPhone);
    EmergencyRequest update(Long id, Long centerId, BloodType bloodType, Integer unitsNeeded,
                            EmergencyUrgency urgency, Integer matchRadius, String contactPhone);
    EmergencyRequest cancel(Long id);
    DonorResponse respond(Long emergencyId, Long donorId);
    DonorResponse acceptResponse(Long responseId, Long slotId);
    DonorResponse declineResponse(Long responseId);
}
