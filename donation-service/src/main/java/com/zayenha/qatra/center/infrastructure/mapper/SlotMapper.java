package com.zayenha.qatra.center.infrastructure.mapper;

import com.zayenha.qatra.center.application.api.dto.SlotDTO;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.web.dto.response.SlotResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class SlotMapper {

    @Autowired
    protected CenterJpaRepository centerJpaRepository;

    public abstract SlotResponse toSlotResponse(Slot slot);
    public abstract SlotDTO toSlotDto(Slot slot);

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(slot.getCenterId()))")
    public abstract SlotEntity toSlotEntity(Slot slot);

    @Mapping(target = "centerId", source = "center.id")
    public abstract Slot toSlotDomain(SlotEntity entity);
}
