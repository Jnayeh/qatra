package com.zayenha.qatra.center.domain.port.in;

import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;

public interface CenterCommandUseCases {
    DonationCenter create(CreateCenterCommand command);

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
}
