package com.zayenha.qatra.donor.infrastructure.web.dto.request;

public record UpdateDonorRequest(
    String displayName,
    String phone
) {}
