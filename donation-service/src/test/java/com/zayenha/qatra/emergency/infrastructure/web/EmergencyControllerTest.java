package com.zayenha.qatra.emergency.infrastructure.web;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.infrastructure.mapper.EmergencyMapper;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.AcceptResponseRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.CreateEmergencyRequest;
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
    @Mock
    private EmergencyMapper mapper;

    private EmergencyController controller;

    @BeforeEach
    void setUp() {
        controller = new EmergencyController(commandUseCases, queryUseCases, mapper);
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
        e.setCenterId(1L);
        e.setBloodType(BloodType.A_POSITIVE);
        e.setUnitsNeeded(2);
        e.setUrgency(EmergencyUrgency.HIGH);
        e.setMatchRadius(50);
        e.setContactPhone("+123");
        e.setStatus(EmergencyStatus.OPEN);
        e.setCreatedAt(Instant.now());
        return e;
    }

    @Test
    void createReturnsCreated() {
        var emergency = anEmergency();
        when(commandUseCases.create(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(emergency);

        var request = new CreateEmergencyRequest(1L, BloodType.A_POSITIVE, 2,
                EmergencyUrgency.HIGH, 50, "+123");
        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void getByIdReturnsEmergency() {
        var emergency = anEmergency();
        when(queryUseCases.findById(1L)).thenReturn(Optional.of(emergency));
        when(mapper.toResponse(emergency)).thenReturn(
                new EmergencyResponse(
                        emergency.getId(),
                        emergency.getCenterId(),
                        emergency.getBloodType(),
                        emergency.getUnitsNeeded(),
                        emergency.getUrgency(),
                        emergency.getMatchRadius(),
                        emergency.getEscalationLevel(),
                        emergency.getContactPhone(),
                        emergency.getStatus(),
                        emergency.getCreatedAt(),
                        emergency.getUpdatedAt(),
                        emergency.getExpiresAt(),
                        emergency.getResolvedAt(),
                        emergency.getResolvedByUserId()
                )
        );

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
    void acceptResponseReturnsOk() {
        var donorResponse = new DonorResponse(1L, 10L);
        donorResponse.setId(1L);
        donorResponse.setSlotId(100L);
        donorResponse.setStatus(ResponseStatus.ACCEPTED);
        when(commandUseCases.acceptResponse(1L, 10L, 100L)).thenReturn(donorResponse);

        var request = new AcceptResponseRequest(100L);
        var response = controller.acceptResponse(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
