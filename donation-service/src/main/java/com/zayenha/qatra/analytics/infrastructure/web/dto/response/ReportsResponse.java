package com.zayenha.qatra.analytics.infrastructure.web.dto.response;

import java.util.Map;

public record ReportsResponse(
    long totalDonations,
    long newDonors,
    long activeCenters,
    double emergencyResponseRate,
    Map<String, Object> additional
) {}
