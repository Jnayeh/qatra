package com.zayenha.qatra.appointment.infrastructure.web.mapper;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentMapperTest {

    @Test
    void toResponseMapsAllFields() {
        var appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDonorId(10L);
        appointment.setSlotId(100L);
        appointment.setCenterId(1000L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setAppointmentDate(LocalDate.of(2030, 6, 15));
        appointment.setStartTime(LocalTime.of(10, 0));
        appointment.setEndTime(LocalTime.of(11, 0));
        appointment.setCreatedAt(Instant.now());

        var response = AppointmentMapper.toResponse(appointment);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.donorId()).isEqualTo(10L);
        assertThat(response.status()).isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void toScreeningResponseMapsAllFields() {
        var screening = new HealthScreening();
        screening.setId(1L);
        screening.setAppointmentId(10L);
        screening.setWeight(70.0);
        screening.setEligible(true);

        var response = AppointmentMapper.toScreeningResponse(screening);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.appointmentId()).isEqualTo(10L);
        assertThat(response.weight()).isEqualTo(70.0);
        assertThat(response.eligible()).isTrue();
    }

    @Test
    void toOutcomeParsesString() {
        assertThat(AppointmentMapper.toOutcome("FULL_DONATION")).isEqualTo(DonationOutcome.FULL_DONATION);
    }
}
