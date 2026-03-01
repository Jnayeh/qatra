package com.zayenha.qatra.center.infrastructure.web.dto.response;

import java.time.LocalDate;

public record ClosureResponse(
    int blockedSlotCount,
    LocalDate date,
    String reason
) {}
