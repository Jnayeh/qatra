package com.zayenha.qatra.center.domain.port.in;

import com.zayenha.qatra.center.domain.model.*;

import java.time.LocalDate;
import java.util.List;

public interface CenterCommandUseCases {
    DonationCenter create(CreateCenterCommand command);
    DonationCenter update(Long id, UpdateCenterCommand command);
    void updateStatus(Long id, CenterStatus status);
    void delete(Long id);
    Slot blockSlot(Long centerId, Long slotId, boolean isBlocked);
    ClosureResult addClosure(Long centerId, ClosureCommand command);
    CenterStaffProfile addStaff(Long centerId, Long userId);
    void removeStaff(Long centerId, Long userId);
    DonationCenter approve(Long centerId, boolean approved, String reason);

    record CreateCenterCommand(
        String name, String address, String city, String country,
        String postalCode, String phone, String email,
        Double latitude, Double longitude,
        FacilityType facilityType, OperatingHours operatingHours,
        Integer totalCapacity, Integer maxRegular, Integer slotPeriod
    ) {}

    record UpdateCenterCommand(
        String name, String address, String city, String country,
        String postalCode, String phone, String email,
        Double latitude, Double longitude,
        FacilityType facilityType, OperatingHours operatingHours,
        Integer totalCapacity, Integer maxRegular, Integer slotPeriod
    ) {}

    record ClosureCommand(
        LocalDate date, String startTime, String endTime,
        boolean allDay, String reason
    ) {}

    record ClosureResult(int blockedSlotCount, LocalDate date, String reason) {}
}
