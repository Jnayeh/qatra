package com.zayenha.qatra.center.infrastructure.mapper;

import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.SlotResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.StaffSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CenterMapper {

    CenterResponse toResponse(DonationCenter center);

    SlotResponse toSlotResponse(Slot slot);

    StaffSummaryResponse toStaffResponse(CenterStaffProfile staff);
}
