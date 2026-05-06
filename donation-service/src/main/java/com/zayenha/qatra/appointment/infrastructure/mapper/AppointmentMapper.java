package com.zayenha.qatra.appointment.infrastructure.mapper;

import com.zayenha.qatra.appointment.application.proxy.AptCenterProxy;
import com.zayenha.qatra.appointment.application.proxy.AptDonorProxy;
import com.zayenha.qatra.appointment.application.proxy.AptEmergencyProxy;
import com.zayenha.qatra.appointment.application.proxy.AptUserProxy;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
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
    public abstract Appointment toDomain(AppointmentEntity entity);

    @Mapping(target = "appointment", expression = "java(appointmentJpaRepository.getReferenceById(screening.getAppointmentId()))")
    @Mapping(target = "donor", expression = "java(donorProxy.getDonorReference(screening.getDonorId()))")
    @Mapping(target = "screenedByStaff", expression = "java(userProxy.getUserReference(screening.getScreenedByStaffId()))")
    public abstract HealthScreeningEntity toScreeningEntity(HealthScreening screening);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "screenedByStaffId", source = "screenedByStaff.id")
    public abstract HealthScreening toScreeningDomain(HealthScreeningEntity entity);
}
