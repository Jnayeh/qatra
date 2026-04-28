package com.zayenha.qatra.appointment.infrastructure.mapper;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    AppointmentResponse toResponse(Appointment appointment);

    HealthScreeningResponse toScreeningResponse(HealthScreening screening);

    default DonationOutcome toOutcome(String value) {
        return DonationOutcome.valueOf(value);
    }
}
