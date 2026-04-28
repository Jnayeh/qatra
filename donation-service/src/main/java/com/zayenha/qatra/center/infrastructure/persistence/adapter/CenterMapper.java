package com.zayenha.qatra.center.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.domain.model.CenterAdminProfile;
import com.zayenha.qatra.center.domain.model.CenterStaffProfile;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterAdminProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CenterMapper {

    @Autowired
    protected CenterJpaRepository centerJpaRepository;

    @Autowired
    protected UserJpaRepository userJpaRepository;

    @Mapping(target = "createdBy", expression = "java(center.getCreatedByUserId() != null ? userJpaRepository.getReferenceById(center.getCreatedByUserId()) : null)")
    public abstract CenterEntity toEntity(DonationCenter center);

    @Mapping(target = "createdByUserId", source = "createdBy.id")
    public abstract DonationCenter toDomain(CenterEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(staff.getCenterId()))")
    @Mapping(target = "user", expression = "java(userJpaRepository.getReferenceById(staff.getUserId()))")
    public abstract CenterStaffProfileEntity toStaffEntity(CenterStaffProfile staff);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "userId", source = "user.id")
    public abstract CenterStaffProfile toStaffDomain(CenterStaffProfileEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(admin.getCenterId()))")
    @Mapping(target = "user", expression = "java(userJpaRepository.getReferenceById(admin.getUserId()))")
    public abstract CenterAdminProfileEntity toAdminEntity(CenterAdminProfile admin);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "userId", source = "user.id")
    public abstract CenterAdminProfile toAdminDomain(CenterAdminProfileEntity entity);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(slot.getCenterId()))")
    public abstract SlotEntity toSlotEntity(Slot slot);

    @Mapping(target = "centerId", source = "center.id")
    public abstract Slot toSlotDomain(SlotEntity entity);
}
