package com.zayenha.qatra.emergency.infrastructure.web;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.infrastructure.mapper.EmergencyMapper;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.AcceptResponseRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;
import com.zayenha.qatra.emergency.domain.model.MatchResultStats;
import com.zayenha.qatra.emergency.domain.model.ResponseStats;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private EmergencyResponse mapResponse(EmergencyRequest e) {
        return new EmergencyResponse(
                e.getId(), e.getCenterId(), e.getBloodType(), e.getUnitsNeeded(), e.getUrgency(),
                e.getMatchRadius(), e.getEscalationLevel(), e.getContactPhone(), e.getStatus(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getExpiresAt(), e.getResolvedAt(),
                e.getResolvedByUserId(), 0, 0, 0, 0, 0);
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
        when(queryUseCases.findById(1L)).thenReturn(emergency);
        when(queryUseCases.getMatchStats(1L)).thenReturn(new MatchResultStats(4, 3));
        when(queryUseCases.getResponseStats(1L)).thenReturn(new ResponseStats(2, 1, 1));
        when(mapper.toResponse(emergency)).thenReturn(mapResponse(emergency));

        var response = controller.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data().id()).isEqualTo(1L);
        assertThat(response.getBody().data().matchedDonorCount()).isEqualTo(4);
        assertThat(response.getBody().data().responseCount()).isEqualTo(2);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(queryUseCases.findById(99L)).thenThrow(new com.zayenha.qatra._shared.exception.NotFoundException("Not found", "EMERGENCY_NOT_FOUND"));

        assertThatThrownBy(() -> controller.getById(99L))
                .isInstanceOf(com.zayenha.qatra._shared.exception.NotFoundException.class);
    }

    @Test
    void getAllReturnsPaginatedResults() {
        var emergency = anEmergency();
        var pageResult = new PageResult<EmergencyRequest>(List.of(emergency), 0, 20, 1, 1);
        when(queryUseCases.findAll(any(SearchCriteria.class), isNull(), isNull())).thenReturn(pageResult);

        var response = controller.getAll(0, 20, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void getAllPassesCenterAndStatusFilters() {
        var emergency = anEmergency();
        var pageResult = new PageResult<EmergencyRequest>(List.of(emergency), 0, 20, 1, 1);
        when(queryUseCases.findAll(any(SearchCriteria.class), eq(5L), eq(EmergencyStatus.OPEN))).thenReturn(pageResult);

        var response = controller.getAll(0, 20, 5L, EmergencyStatus.OPEN);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void cancelReturnsOk() {
        var emergency = anEmergency();
        when(commandUseCases.cancel(1L)).thenReturn(emergency);
        when(mapper.toResponse(emergency)).thenReturn(mapResponse(emergency));

        var response = controller.cancel(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void escalateReturnsOk() {
        var emergency = anEmergency();
        when(commandUseCases.escalate(1L)).thenReturn(emergency);
        when(mapper.toResponse(emergency)).thenReturn(mapResponse(emergency));

        var response = controller.escalate(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
