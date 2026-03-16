package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

public record MetricsResponse(
    String metricName,
    Long count
) {}
