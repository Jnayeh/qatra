package com.zayenha.qatra.appointment.infrastructure.web.mapper;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;

public final class AppointmentMapper {

    private AppointmentMapper() {}

    public static AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
            appointment.getId(),
            appointment.getDonorId(),
            appointment.getSlotId(),
            appointment.getCenterId(),
            appointment.getStatus(),
            appointment.getAppointmentDate(),
            appointment.getStartTime(),
            appointment.getEndTime(),
            appointment.getCheckInTime(),
            appointment.getCompletedAt(),
            appointment.getOutcome(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getUpdatedAt()
        );
    }

    public static HealthScreeningResponse toScreeningResponse(HealthScreening screening) {
        return new HealthScreeningResponse(
            screening.getId(),
            screening.getAppointmentId(),
            screening.getWeight(),
            screening.getBloodPressure(),
            screening.getHemoglobin(),
            screening.getTemperature(),
            screening.getEligible(),
            screening.getNotes(),
            screening.getCreatedAt()
        );
    }

    public static DonationOutcome toOutcome(String value) {
        return DonationOutcome.valueOf(value);
    }
}
