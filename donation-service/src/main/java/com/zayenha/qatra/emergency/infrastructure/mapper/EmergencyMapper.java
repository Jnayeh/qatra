package com.zayenha.qatra.emergency.infrastructure.mapper;

import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.DonorResponseResponse;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmergencyMapper {

    EmergencyResponse toResponse(EmergencyRequest request);

    DonorResponseResponse toResponse(DonorResponse response);
}
