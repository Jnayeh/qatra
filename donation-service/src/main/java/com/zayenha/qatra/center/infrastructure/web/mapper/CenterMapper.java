package com.zayenha.qatra.center.infrastructure.web.mapper;

import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterResponse;

public class CenterMapper {

    public static CenterResponse toResponse(DonationCenter center) {
        return new CenterResponse(
            center.getId(), center.getName(), center.getAddress(),
            center.getCity(), center.getCountry(), center.getPostalCode(),
            center.getPhone(), center.getEmail(), center.getLatitude(),
            center.getLongitude(), center.getFacilityType(),
            center.getOperatingHours(), center.getStatus(),
            center.getTotalCapacity(), center.getMaxRegular(),
            center.getSlotPeriod(), center.getCreatedAt(), center.getUpdatedAt()
        );
    }
}
