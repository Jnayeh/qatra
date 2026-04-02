package com.zayenha.qatra.system.infrastructure.web.mapper;

import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.response.GDPRDeletionResponse;

public final class SystemMapper {

    private SystemMapper() {}

    public static GDPRDeletionResponse toResponse(GDPRDeletionRequest request) {
        return new GDPRDeletionResponse(
            request.getId(), request.getUserId(), request.getReason(),
            request.getStatus(), request.getRequestedAt(), request.getProcessedAt(), request.getProcessedBy()
        );
    }
}
