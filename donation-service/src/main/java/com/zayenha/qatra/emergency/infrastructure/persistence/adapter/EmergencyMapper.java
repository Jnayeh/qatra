package com.zayenha.qatra.emergency.infrastructure.persistence.adapter;

import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.SlotJpaRepository;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.MatchResult;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.MatchResultEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class EmergencyMapper {

    @Autowired
    protected CenterJpaRepository centerJpaRepository;

    @Autowired
    protected UserJpaRepository userJpaRepository;

    @Autowired
    protected DonorJpaRepository donorJpaRepository;

    @Autowired
    protected SlotJpaRepository slotJpaRepository;

    @Autowired
    protected EmergencyJpaRepository emergencyJpaRepository;

    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(request.getCenterId()))")
    @Mapping(target = "createdByStaff", expression = "java(userJpaRepository.getReferenceById(request.getCreatedByStaffId()))")
    @Mapping(target = "resolvedBy", expression = "java(request.getResolvedByUserId() != null ? userJpaRepository.getReferenceById(request.getResolvedByUserId()) : null)")
    public abstract EmergencyRequestEntity toEntity(EmergencyRequest request);

    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "createdByStaffId", source = "createdByStaff.id")
    @Mapping(target = "resolvedByUserId", source = "resolvedBy.id")
    public abstract EmergencyRequest toDomain(EmergencyRequestEntity entity);

    @Mapping(target = "emergency", expression = "java(emergencyJpaRepository.getReferenceById(response.getEmergencyId()))")
    @Mapping(target = "donor", expression = "java(donorJpaRepository.getReferenceById(response.getDonorId()))")
    @Mapping(target = "slot", expression = "java(response.getSlotId() != null ? slotJpaRepository.getReferenceById(response.getSlotId()) : null)")
    public abstract DonorResponseEntity toResponseEntity(DonorResponse response);

    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "slotId", source = "slot.id")
    public abstract DonorResponse toResponseDomain(DonorResponseEntity entity);

    @Mapping(target = "emergency", expression = "java(emergencyJpaRepository.getReferenceById(result.getEmergencyId()))")
    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(result.getCenterId()))")
    @Mapping(target = "donor", expression = "java(donorJpaRepository.getReferenceById(result.getDonorId()))")
    public abstract MatchResultEntity toMatchResultEntity(MatchResult result);

    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "donorId", source = "donor.id")
    public abstract MatchResult toMatchResultDomain(MatchResultEntity entity);
}
