package com.zayenha.qatra.emergency.infrastructure.web;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.AcceptResponseRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.CreateEmergencyRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.UpdateEmergencyRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmergencyControllerTest {

    @Mock
    private EmergencyCommandUseCases commandUseCases;
    @Mock
    private EmergencyQueryUseCases queryUseCases;

    private EmergencyController controller;

    @BeforeEach
    void setUp() {
        controller = new EmergencyController(commandUseCases, queryUseCases);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(10L, null, java.util.List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private EmergencyRequest anEmergency() {
        var e = new EmergencyRequest();
        e.setId(1L);
        e.setPatientName("John");
        e.setBloodType(BloodType.A_POSITIVE);
        e.setUnitsNeeded(2);
        e.setUrgency(EmergencyUrgency.HIGH);
        e.setHospital("General");
        e.setStatus(EmergencyStatus.OPEN);
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void createReturnsCreated() {
        var emergency = anEmergency();
        when(commandUseCases.create(anyString(), any(), anyInt(), any(), anyString(), anyDouble(), anyDouble(), anyString()))
                .thenReturn(emergency);

        var request = new CreateEmergencyRequest("John", BloodType.A_POSITIVE, 2,
                EmergencyUrgency.HIGH, "General", 40.71, -74.00, "+123");
        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().patientName()).isEqualTo("John");
    }

    @Test
    void getByIdReturnsEmergency() {
        var emergency = anEmergency();
        when(queryUseCases.findById(1L)).thenReturn(Optional.of(emergency));

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
        var emergency = anEmergency();
        var pageResult = new PageResult<EmergencyRequest>(List.of(emergency), 0, 20, 1, 1);
        when(queryUseCases.findAll(any(SearchCriteria.class))).thenReturn(pageResult);

        var response = controller.getAll(0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void respondReturnsCreated() {
        var donorResponse = new DonorResponse();
        donorResponse.setId(1L);
        donorResponse.setEmergencyId(1L);
        donorResponse.setDonorId(10L);
        donorResponse.setStatus(ResponseStatus.PENDING);
        when(commandUseCases.respond(1L, 10L)).thenReturn(donorResponse);

        var response = controller.respond(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().data().donorId()).isEqualTo(10L);
    }

    @Test
    void acceptResponseReturnsOk() {
        var donorResponse = new DonorResponse();
        donorResponse.setId(1L);
        donorResponse.setStatus(ResponseStatus.ACCEPTED);
        donorResponse.setSlotId(100L);
        when(commandUseCases.acceptResponse(1L, 100L)).thenReturn(donorResponse);

        var request = new AcceptResponseRequest(100L);
        var response = controller.acceptResponse(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().status()).isEqualTo(ResponseStatus.ACCEPTED);
    }
}
