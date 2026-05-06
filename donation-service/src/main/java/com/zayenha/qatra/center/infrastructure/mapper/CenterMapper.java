package com.zayenha.qatra.center.infrastructure.mapper;

import com.zayenha.qatra.center.application.proxy.CenterUserProxy;
import com.zayenha.qatra.center.domain.model.CenterAdminProfile;
import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterAdminProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.SlotResponse;
import com.zayenha.qatra.center.infrastructure.web.dto.response.StaffSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CenterMapper {

    @Autowired
    protected CenterJpaRepository centerJpaRepository;

    @Autowired
    protected CenterUserProxy userProxy;

    public abstract CenterResponse toResponse(DonationCenter center);

    public abstract SlotResponse toSlotResponse(Slot slot);

    public abstract StaffSummaryResponse toStaffResponse(CenterStaffProfile staff);

    @Mapping(target = "createdBy", expression = "java(center.getCreatedByUserId() != null ? userProxy.getUserReference(center.getCreatedByUserId()) : null)")
    public abstract CenterEntity toEntity(DonationCenter center);

    @Mapping(target = "createdByUserId", source = "createdBy.id")
    public abstract DonationCenter toDomain(CenterEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(staff.getCenterId()))")
    @Mapping(target = "user", expression = "java(userProxy.getUserReference(staff.getUserId()))")
    public abstract CenterStaffProfileEntity toStaffEntity(CenterStaffProfile staff);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "userId", source = "user.id")
    public abstract CenterStaffProfile toStaffDomain(CenterStaffProfileEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(admin.getCenterId()))")
    @Mapping(target = "user", expression = "java(userProxy.getUserReference(admin.getUserId()))")
    public abstract CenterAdminProfileEntity toAdminEntity(CenterAdminProfile admin);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "userId", source = "user.id")
    public abstract CenterAdminProfile toAdminDomain(CenterAdminProfileEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(slot.getCenterId()))")
    public abstract SlotEntity toSlotEntity(Slot slot);

    @Mapping(target = "centerId", source = "center.id")
    public abstract Slot toSlotDomain(SlotEntity entity);
}
