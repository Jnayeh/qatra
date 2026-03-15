package com.zayenha.qatra.emergency.infrastructure.web.mapper;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmergencyMapperTest {

    @Test
    void toResponseMapsEmergencyRequest() {
        var request = new EmergencyRequest();
        request.setId(1L);
        request.setPatientName("John");
        request.setBloodType(BloodType.A_POSITIVE);
        request.setStatus(EmergencyStatus.OPEN);

        var response = EmergencyMapper.toResponse(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.patientName()).isEqualTo("John");
        assertThat(response.bloodType()).isEqualTo(BloodType.A_POSITIVE);
    }

    @Test
    void toResponseMapsDonorResponse() {
        var response = new DonorResponse();
        response.setId(1L);
        response.setEmergencyId(10L);
        response.setDonorId(100L);
        response.setStatus(ResponseStatus.PENDING);

        var dto = EmergencyMapper.toResponse(response);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.emergencyId()).isEqualTo(10L);
        assertThat(dto.status()).isEqualTo(ResponseStatus.PENDING);
    }
}
