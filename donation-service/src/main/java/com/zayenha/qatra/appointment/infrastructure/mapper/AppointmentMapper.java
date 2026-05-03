package com.zayenha.qatra.appointment.infrastructure.mapper;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.HealthScreeningEntity;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.center.infrastructure.persistence.repository.CenterJpaRepository;
import com.zayenha.qatra.center.infrastructure.persistence.repository.SlotJpaRepository;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class AppointmentMapper {

    @Autowired
    protected DonorJpaRepository donorJpaRepository;

    @Autowired
    protected SlotJpaRepository slotJpaRepository;

    @Autowired
    protected CenterJpaRepository centerJpaRepository;

    @Autowired
    protected EmergencyJpaRepository emergencyJpaRepository;

    @Autowired
    protected UserJpaRepository userJpaRepository;

    @Autowired
    protected AppointmentJpaRepository appointmentJpaRepository;

    public abstract AppointmentResponse toResponse(Appointment appointment);

    public abstract HealthScreeningResponse toScreeningResponse(HealthScreening screening);

    public DonationOutcome toOutcome(String value) {
        return DonationOutcome.valueOf(value);
    }

    @Mapping(target = "donor", expression = "java(donorJpaRepository.getReferenceById(appointment.getDonorId()))")
    @Mapping(target = "slot", expression = "java(slotJpaRepository.getReferenceById(appointment.getSlotId()))")
    @Mapping(target = "center", expression = "java(centerJpaRepository.getReferenceById(appointment.getCenterId()))")
    @Mapping(target = "emergency", expression = "java(appointment.getEmergencyId() != null ? emergencyJpaRepository.getReferenceById(appointment.getEmergencyId()) : null)")
    @Mapping(target = "completedByStaff", expression = "java(appointment.getCompletedByStaffId() != null ? userJpaRepository.getReferenceById(appointment.getCompletedByStaffId()) : null)")
    public abstract AppointmentEntity toEntity(Appointment appointment);

    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "slotId", source = "slot.id")
    @Mapping(target = "centerId", source = "center.id")
    @Mapping(target = "emergencyId", source = "emergency.id")
    @Mapping(target = "completedByStaffId", source = "completedByStaff.id")
    public abstract Appointment toDomain(AppointmentEntity entity);

    @Mapping(target = "appointment", expression = "java(appointmentJpaRepository.getReferenceById(screening.getAppointmentId()))")
    @Mapping(target = "donor", expression = "java(donorJpaRepository.getReferenceById(screening.getDonorId()))")
    @Mapping(target = "screenedByStaff", expression = "java(userJpaRepository.getReferenceById(screening.getScreenedByStaffId()))")
    public abstract HealthScreeningEntity toScreeningEntity(HealthScreening screening);

    @Mapping(target = "appointmentId", source = "appointment.id")
    @Mapping(target = "donorId", source = "donor.id")
    @Mapping(target = "screenedByStaffId", source = "screenedByStaff.id")
    public abstract HealthScreening toScreeningDomain(HealthScreeningEntity entity);
}
