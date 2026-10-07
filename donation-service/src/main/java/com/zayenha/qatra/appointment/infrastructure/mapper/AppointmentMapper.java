package com.zayenha.qatra.appointment.infrastructure.mapper;

import com.zayenha.qatra.appointment.application.proxy.AptCenterProxy;
import com.zayenha.qatra.appointment.application.proxy.AptDonorProxy;
import com.zayenha.qatra.appointment.application.proxy.AptEmergencyProxy;
import com.zayenha.qatra.appointment.application.proxy.AptUserProxy;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra._shared.domain.enums.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.HealthScreeningEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AppointmentMapper {

    @Autowired
    protected AptCenterProxy centerProxy;

    @Autowired
    protected AptDonorProxy donorProxy;

    @Autowired
    protected AptUserProxy userProxy;

    @Autowired
    protected AptEmergencyProxy emergencyProxy;

    @Autowired
    protected AppointmentJpaRepository appointmentJpaRepository;

    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "slotId", source = "slot.id")
    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "completedByStaffId", source = "completedByStaff.id")
    @Mapping(target = "slotTime", source = "slot.startTime")
    @Mapping(target = "slotDate", source = "slot.date")
    @Mapping(target = "donorFirstName", source = "donor.user.firstName")
    @Mapping(target = "donorLastName", source = "donor.user.familyName")
    @Mapping(target = "donorDisplayName", source = "donor.user.displayName")
    @Mapping(target = "donorPhone", source = "donor.user.phone")
    @Mapping(target = "donorEmail", source = "donor.user.email")
    @Mapping(target = "donorCity", source = "donor.city")
    @Mapping(target = "slotName", expression = "java(entity.getSlot() != null ? (entity.getSlot().getStartTime() + \" - \" + entity.getSlot().getEndTime()) : null)")
    public abstract AppointmentResponse toResponseEntity(AppointmentEntity entity);

    public abstract AppointmentResponse toResponse(Appointment appointment);

    public abstract HealthScreeningResponse toScreeningResponse(HealthScreening screening);

    public DonationOutcome toOutcome(String value) {
        return DonationOutcome.valueOf(value);
    }

    @Mapping(target = "donor", expression = "java(donorProxy.getDonorReference(appointment.getDonorId()))")
    @Mapping(target = "slot", expression = "java(centerProxy.getSlotReference(appointment.getSlotId()))")
    @Mapping(target = "center", expression = "java(centerProxy.getCenterReference(appointment.getCenterId()))")
    @Mapping(target = "emergency", expression = "java(appointment.getEmergencyId() != null ? emergencyProxy.getEmergencyReference(appointment.getEmergencyId()) : null)")
    @Mapping(target = "completedByStaff", expression = "java(appointment.getCompletedByStaffId() != null ? userProxy.getUserReference(appointment.getCompletedByStaffId()) : null)")
    public abstract AppointmentEntity toEntity(Appointment appointment);

    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "slotId", source = "slot.id")
    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "completedByStaffId", source = "completedByStaff.id")
    @Mapping(target = "slotDate", source = "slot.date")
    @Mapping(target = "slotTime", source = "slot.startTime")
    @Mapping(target = "slotName", expression = "java(entity.getSlot() != null ? (entity.getSlot().getStartTime() + \" - \" + entity.getSlot().getEndTime()) : null)")
    @Mapping(target = "donorFirstName", source = "donor.user.firstName")
    @Mapping(target = "donorLastName", source = "donor.user.familyName")
    @Mapping(target = "donorDisplayName", source = "donor.user.displayName")
    @Mapping(target = "donorPhone", source = "donor.user.phone")
    @Mapping(target = "donorEmail", source = "donor.user.email")
    @Mapping(target = "donorCity", source = "donor.city")
    public abstract Appointment toDomain(AppointmentEntity entity);

    @Mapping(target = "screenedByStaff", expression = "java(userProxy.getUserReference(screening.getScreenedByStaffId()))")
    public abstract HealthScreeningEntity toScreeningEntity(HealthScreening screening);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "screenedByStaffId", source = "screenedByStaff.id")
    public abstract HealthScreening toScreeningDomain(HealthScreeningEntity entity);
}
