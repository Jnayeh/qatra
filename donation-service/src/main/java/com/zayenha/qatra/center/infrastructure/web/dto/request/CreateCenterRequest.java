package com.zayenha.qatra.center.infrastructure.web.dto.request;

import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCenterRequest(
    @NotBlank String name,
    @NotBlank String address,
    @NotBlank String city,
    @NotBlank String country,
    String postalCode,
    @NotBlank String phone,
    @NotBlank String email,
    @NotNull Double latitude,
    @NotNull Double longitude,
    @NotNull FacilityType facilityType,
    @NotNull OperatingHours operatingHours,
    @NotNull Integer totalCapacity,
    @NotNull Integer maxRegular,
    @NotNull Integer slotPeriod
) {}
