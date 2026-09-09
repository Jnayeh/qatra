package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.util.List;
import java.util.Map;

public record ImpactResponse(
    int totalDonations,
    List<String> milestones,
    Map<String, Integer> donationsByType,
    Map<String, Integer> monthlyDonations
) {}
