package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.util.List;

public record ImpactResponse(
    int totalDonations,
    List<String> milestones
) {}
