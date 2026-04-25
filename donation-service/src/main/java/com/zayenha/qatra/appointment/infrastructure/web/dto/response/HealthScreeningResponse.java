package com.zayenha.qatra.appointment.infrastructure.web.dto.response;

import java.time.Instant;

public record HealthScreeningResponse(
    Long id,
    Long appointmentId,
    Long donorId,
    Long screenedByStaffId,
    Double weight,
    String bloodPressure,
    Double hemoglobin,
    Double temperature,
    Boolean eligible,
    String notes,
    Instant screenedAt
) {}
