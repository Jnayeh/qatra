package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

public record SystemConfigResponse(
    String key,
    Object value,
    String description
) {}