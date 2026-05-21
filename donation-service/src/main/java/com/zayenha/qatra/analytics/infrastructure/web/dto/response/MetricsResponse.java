package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

public record MetricsResponse(
    String metricName,
    long total,
    long today,
    long thisWeek,
    long thisMonth
) {}
