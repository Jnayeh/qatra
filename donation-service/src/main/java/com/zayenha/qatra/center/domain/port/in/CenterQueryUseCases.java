package com.zayenha.qatra.center.domain.port.in;

import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;

import java.time.LocalDate;
import java.util.List;

public interface CenterQueryUseCases {
    DonationCenter getById(Long id);
    DonationCenter getById(Long id, boolean fetchJoins);
    PageResult<DonationCenter> getAll(SearchCriteria criteria);
    PageResult<DonationCenter> getPending(SearchCriteria criteria);
    List<Slot> getSlots(Long centerId, LocalDate date, String slotType, boolean fetchJoins);
    List<CenterStaffProfile> getStaff(Long centerId);
    String generateCenterReport(Long centerId, LocalDate startDate, LocalDate endDate);
}
