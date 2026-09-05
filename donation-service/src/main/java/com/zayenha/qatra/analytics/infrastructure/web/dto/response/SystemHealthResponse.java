package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

import java.util.List;
import java.util.Map;

public record SystemHealthResponse(
    List<ServiceStatus> services,
    ErrorRateInfo errorRates,
    ApiUsageInfo apiUsage,
    DbPoolInfo dbPoolStats
) {
    public record ServiceStatus(String name, String status, String details) {}
    public record ErrorRateInfo(long total, Map<String, Long> byEndpoint) {}
    public record ApiUsageInfo(double requestsPerMinute, List<String> topEndpoints) {}
    public record DbPoolInfo(int active, int idle, int max) {}
}