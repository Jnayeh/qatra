package com.zayenha.qatra.center.infrastructure.web.mapper;

import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.SlotResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.StaffSummaryResponse;

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

    public static SlotResponse toSlotResponse(Slot slot) {
        return new SlotResponse(
            slot.getId(), slot.getCenterId(), slot.getDate(),
            slot.getStartTime(), slot.getEndTime(),
            slot.getMaxBookings(), slot.getMaxRegularBookings(),
            slot.getBookedCount(), slot.getRegularBookedCount(),
            slot.isBlocked()
        );
    }

    public static StaffSummaryResponse toStaffResponse(CenterStaffProfile staff) {
        return new StaffSummaryResponse(
            staff.getId(), staff.getUserId(), staff.getCenterId(),
            staff.isVerified(), staff.getCreatedAt()
        );
    }
}
