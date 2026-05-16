package com.zayenha.qatra.donor.infrastructure.web.dto.response;

import java.time.LocalDate;

public record CertificateResponse(
    Long id,
    Long appointmentId,
    String donorName,
    String centerName,
    Integer mlCollected,
    LocalDate donationDate,
    String downloadUrl
) {}
