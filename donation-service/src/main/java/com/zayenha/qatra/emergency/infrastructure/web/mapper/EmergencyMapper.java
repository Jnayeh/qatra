package com.zayenha.qatra.emergency.infrastructure.web.mapper;

import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.DonorResponseResponse;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;

public final class EmergencyMapper {

    private EmergencyMapper() {}

    public static EmergencyResponse toResponse(EmergencyRequest request) {
        return new EmergencyResponse(
            request.getId(), request.getPatientName(), request.getBloodType(),
            request.getUnitsNeeded(), request.getUrgency(), request.getHospital(),
            request.getHospitalAddress(), request.getLatitude(), request.getLongitude(),
            request.getContactPhone(), request.getStatus(), request.getCreatedAt(),
            request.getUpdatedAt(), request.getExpiresAt()
        );
    }

    public static DonorResponseResponse toResponse(DonorResponse response) {
        return new DonorResponseResponse(
            response.getId(), response.getEmergencyId(), response.getDonorId(),
            response.getSlotId(), response.getStatus(), response.getRespondedAt(),
            response.getCreatedAt()
        );
    }
}
