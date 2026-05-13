package com.zayenha.qatra.appointment.infrastructure.web.mapper;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;
import com.zayenha.qatra.appointment.infrastructure.mapper.AppointmentMapper;
import com.zayenha.qatra.appointment.infrastructure.mapper.AppointmentMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentMapperTest {

    private AppointmentMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AppointmentMapperImpl();
    }

    @Test
    void toResponseMapsAllFields() {
        var appointment = new Appointment(10L, 100L, 1000L, null, null, null);
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(Instant.now());

        var response = mapper.toResponse(appointment);

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

        var response = mapper.toScreeningResponse(screening);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.appointmentId()).isEqualTo(10L);
        assertThat(response.weight()).isEqualTo(70.0);
        assertThat(response.eligible()).isTrue();
    }

    @Test
    void toOutcomeParsesString() {
        assertThat(mapper.toOutcome("COMPLETED")).isEqualTo(DonationOutcome.COMPLETED);
    }
}
