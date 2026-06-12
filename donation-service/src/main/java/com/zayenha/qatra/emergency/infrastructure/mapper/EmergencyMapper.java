package com.zayenha.qatra.emergency.infrastructure.mapper;

import com.zayenha.qatra.emergency.application.proxy.EmergencyCenterProxy;
import com.zayenha.qatra.emergency.application.proxy.EmergencyDonorProxy;
import com.zayenha.qatra.emergency.application.proxy.EmergencyUserProxy;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.MatchResult;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.MatchResultEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.DonorResponseDTO;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class EmergencyMapper {

    @Autowired
    protected EmergencyCenterProxy centerProxy;

    @Autowired
    protected EmergencyUserProxy userProxy;

    @Autowired
    protected EmergencyDonorProxy donorProxy;

    @Autowired
    protected EmergencyJpaRepository emergencyJpaRepository;

    public abstract EmergencyResponse toResponse(EmergencyRequest request);

    public abstract DonorResponseDTO toResponse(DonorResponse response);

    @Mapping(target = "center", expression = "java(centerProxy.getCenterReference(request.getCenterId()))")
    @Mapping(target = "createdByStaff", expression = "java(userProxy.getUserReference(request.getCreatedByStaffId()))")
    @Mapping(target = "resolvedBy", expression = "java(request.getResolvedByUserId() != null ? userProxy.getUserReference(request.getResolvedByUserId()) : null)")
    public abstract EmergencyRequestEntity toEntity(EmergencyRequest request);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "createdByStaffId", source = "createdByStaff.id")
    @Mapping(target = "resolvedByUserId", source = "resolvedBy.id")
    public abstract EmergencyRequest toDomain(EmergencyRequestEntity entity);

    @Mapping(target = "emergency", expression = "java(emergencyJpaRepository.getReferenceById(response.getEmergencyId()))")
    @Mapping(target = "donor", expression = "java(donorProxy.getDonorReference(response.getDonorId()))")
    @Mapping(target = "slot", expression = "java(response.getSlotId() != null ? centerProxy.getSlotReference(response.getSlotId()) : null)")
    public abstract DonorResponseEntity toResponseEntity(DonorResponse response);

    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "userId", source = "donor.id")
    @Mapping(target = "slotId", source = "slot.id")
    public abstract DonorResponse toResponseDomain(DonorResponseEntity entity);

    @Mapping(target = "emergency", expression = "java(emergencyJpaRepository.getReferenceById(result.getEmergencyId()))")
    @Mapping(target = "center", expression = "java(centerProxy.getCenterReference(result.getCenterId()))")
    @Mapping(target = "donor", expression = "java(donorProxy.getDonorReference(result.getDonorId()))")
    public abstract MatchResultEntity toMatchResultEntity(MatchResult result);

    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "userId", source = "donor.id")
    public abstract MatchResult toMatchResultDomain(MatchResultEntity entity);
}
