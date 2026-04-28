package com.zayenha.qatra.system.infrastructure.mapper;

import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.response.GDPRDeletionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemMapper {

    GDPRDeletionResponse toResponse(GDPRDeletionRequest request);
}
