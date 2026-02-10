package com.zayenha.qatra.center.domain.port.in;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;

public interface CenterCommandUseCases {
    DonationCenter create(CreateCenterCommand command);
    DonationCenter update(Long id, UpdateCenterCommand command);
    void updateStatus(Long id, CenterStatus status);
    void delete(Long id);

    record CreateCenterCommand(
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
        Integer totalCapacity,
        Integer maxRegular,
        Integer slotPeriod
    ) {}

    record UpdateCenterCommand(
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
        Integer totalCapacity,
        Integer maxRegular,
        Integer slotPeriod
    ) {}
}
