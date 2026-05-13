package com.zayenha.qatra.appointment.infrastructure.web;

import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import com.zayenha.qatra.appointment.infrastructure.mapper.AppointmentMapper;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.CompleteAppointmentRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.CreateAppointmentRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.ScreeningRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentCommandUseCases commandUseCases;
    @Mock
    private AppointmentQueryUseCases queryUseCases;
    @Mock
    private AppointmentMapper mapper;

    private AppointmentController controller;

    @BeforeEach
    void setUp() {
        controller = new AppointmentController(commandUseCases, queryUseCases, mapper);
    }

    private Appointment anAppointment() {
        var a = new Appointment();
        a.setId(1L);
        a.setDonorId(1L);
        a.setSlotId(100L);
        a.setCenterId(1000L);
        a.setStatus(AppointmentStatus.SCHEDULED);
        a.setCreatedAt(Instant.now());
        return a;
    }

    private AppointmentResponse aResponse() {
        return new AppointmentResponse(1L, 1L, 100L, 1000L, null, null,
            AppointmentType.REGULAR, AppointmentStatus.SCHEDULED, null, null,
            null, null, null, null, null, null, null, null, Instant.now(), Instant.now());
    }

    private HealthScreeningResponse aScreeningResponse() {
        return new HealthScreeningResponse(1L, 1L, null, null, null, null, null, null, true, null, null);
    }

    @Test
    void bookReturnsCreated() {
        var appointment = anAppointment();
        when(commandUseCases.book(1L, 100L, null, AppointmentType.REGULAR)).thenReturn(appointment);
        when(mapper.toResponse(appointment)).thenReturn(aResponse());

        var request = new CreateAppointmentRequest( AppointmentType.REGULAR, 1L, 100L, null);
        var response = controller.book(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().donorId()).isEqualTo(1L);
    }

    @Test
    void checkInReturnsOk() {
        var appointment = anAppointment();
        appointment.setStatus(AppointmentStatus.CHECKED_IN);
        when(commandUseCases.checkIn(1L)).thenReturn(appointment);
        when(mapper.toResponse(appointment)).thenReturn(new AppointmentResponse(1L, 1L, 100L, 1000L, null, null,
            AppointmentType.REGULAR, AppointmentStatus.CHECKED_IN, null, null,
            null, null, null, null, null, null, null, null, Instant.now(), Instant.now()));

        var response = controller.checkIn(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().status()).isEqualTo(AppointmentStatus.CHECKED_IN);
    }

    @Test
    void completeReturnsOk() {
        var appointment = anAppointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setOutcome(DonationOutcome.COMPLETED);
        when(mapper.toOutcome("FULL_DONATION")).thenReturn(DonationOutcome.COMPLETED);
        when(commandUseCases.complete(eq(1L), eq(DonationOutcome.COMPLETED), eq("Good"))).thenReturn(appointment);
        when(mapper.toResponse(appointment)).thenReturn(new AppointmentResponse(1L, 1L, 100L, 1000L, null, null,
            AppointmentType.REGULAR, AppointmentStatus.COMPLETED, null, DonationOutcome.COMPLETED,
            null, null, null, null, null, null, null, null, Instant.now(), Instant.now()));

        var request = new CompleteAppointmentRequest("FULL_DONATION", "Good");
        var response = controller.complete(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().outcome()).isEqualTo(DonationOutcome.COMPLETED);
    }

    @Test
    void cancelReturnsOk() {
        var appointment = anAppointment();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        when(commandUseCases.cancel(1L)).thenReturn(appointment);
        when(mapper.toResponse(appointment)).thenReturn(new AppointmentResponse(1L, 1L, 100L, 1000L, null, null,
            AppointmentType.REGULAR, AppointmentStatus.CANCELLED, null, null,
            null, null, null, null, null, null, null, null, Instant.now(), Instant.now()));

        var response = controller.cancel(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().status()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void getByIdReturnsAppointment() {
        var appointment = anAppointment();
        when(queryUseCases.findById(1L)).thenReturn(Optional.of(appointment));
        when(mapper.toResponse(appointment)).thenReturn(aResponse());

        var response = controller.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().id()).isEqualTo(1L);
    }

    @Test
    void getByIdReturns404WhenNotFound() {
        when(queryUseCases.findById(99L)).thenReturn(Optional.empty());

        var response = controller.getById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllReturnsPaginatedResults() {
        var appointment = anAppointment();
        var pageResult = new PageResult<Appointment>(List.of(appointment), 0, 20, 1, 1);
        when(queryUseCases.findAll(any(SearchCriteria.class))).thenReturn(pageResult);

        var response = controller.getAll(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getByDonorReturnsAppointments() {
        var appointment = anAppointment();
        when(queryUseCases.findByDonorId(1L)).thenReturn(List.of(appointment));

        var response = controller.getByDonor(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void saveScreeningReturnsOk() {
        var screening = new HealthScreening();
        screening.setId(1L);
        screening.setAppointmentId(1L);
        screening.setEligible(true);
        when(commandUseCases.saveScreening(eq(1L), eq(70.0), eq("120/80"), eq(14.5), eq(36.6), eq(true), eq("Fit")))
                .thenReturn(screening);
        when(mapper.toScreeningResponse(screening)).thenReturn(aScreeningResponse());

        var request = new ScreeningRequest(70.0, "120/80", 14.5, 36.6, true, "Fit");
        var response = controller.saveScreening(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().eligible()).isTrue();
    }

    @Test
    void getScreeningReturnsScreening() {
        var screening = new HealthScreening();
        screening.setId(1L);
        screening.setAppointmentId(1L);
        when(queryUseCases.findScreeningByAppointmentId(1L)).thenReturn(Optional.of(screening));
        when(mapper.toScreeningResponse(screening)).thenReturn(aScreeningResponse());

        var response = controller.getScreening(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().appointmentId()).isEqualTo(1L);
    }
}
