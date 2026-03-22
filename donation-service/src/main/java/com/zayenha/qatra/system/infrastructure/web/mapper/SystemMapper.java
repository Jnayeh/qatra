package com.zayenha.qatra.system.infrastructure.web.mapper;

import com.zayenha.qatra.system.domain.model.FeatureFlag;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.SystemConfig;
import com.zayenha.qatra.system.infrastructure.web.dto.response.FeatureFlagResponse;
import com.zayenha.qatra.system.infrastructure.web.dto.response.GDPRDeletionResponse;
import com.zayenha.qatra.system.infrastructure.web.dto.response.SystemConfigResponse;

public final class SystemMapper {

    private SystemMapper() {}

    public static SystemConfigResponse toResponse(SystemConfig config) {
        return new SystemConfigResponse(
            config.getId(), config.getConfigKey(), config.getConfigValue(),
            config.getDescription(), config.getCreatedAt(), config.getUpdatedAt()
        );
    }

    public static FeatureFlagResponse toResponse(FeatureFlag flag) {
        return new FeatureFlagResponse(
            flag.getId(), flag.getFlagName(), flag.isEnabled(),
            flag.getDescription(), flag.getCreatedAt(), flag.getUpdatedAt()
        );
    }

    public static GDPRDeletionResponse toResponse(GDPRDeletionRequest request) {
        return new GDPRDeletionResponse(
            request.getId(), request.getUserId(), request.getReason(),
            request.getStatus(), request.getRequestedAt(), request.getProcessedAt(), request.getProcessedBy()
        );
    }
}
