package com.zayenha.qatra.center.infrastructure.web.dto.response;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;

import java.time.Instant;

public record CenterResponse(
    Long id,
    String name,
    String address,
    String city,
    String country,
    String postalCode,
    String phone,
    String email,
    Double latitude,
    Double longitude,
    FacilityType facilityType,
    OperatingHours operatingHours,
    CenterStatus status,
    Integer totalCapacity,
    Integer maxRegular,
    Integer slotPeriod,
    Instant createdAt,
    Instant updatedAt
) {}
