package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.infrastructure.web.dto.request.CreateCenterRequest;
import com.zayenha.qatra.center.infrastructure.web.dto.request.UpdateCenterRequest;
import com.zayenha.qatra.center.infrastructure.web.dto.request.UpdateCenterStatusRequest;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.exception.GlobalExceptionHandler;
import com.zayenha.qatra._shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterControllerTest {

    @Mock
    private CenterCommandUseCases commandUseCases;
    @Mock
    private CenterQueryUseCases queryUseCases;

    private CenterController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new CenterController(commandUseCases, queryUseCases);
        exceptionHandler = new GlobalExceptionHandler();
    }

    private DonationCenter aCenter() {
        var op = new OperatingHours(
            new OperatingHours.DaySchedule(LocalTime.of(8, 0), LocalTime.of(17, 0)),
            null, null, null, null, null, null, null
        );
        var center = new DonationCenter("Main Center", "123 Street", "City", "Country",
                "12345", "1234567890", "center@test.com",
                40.7128, -74.0060, FacilityType.BLOOD_BANK, op,
                100, 50, 30);
        center.setId(1L);
        center.setStatus(CenterStatus.ACTIVE);
        center.setCreatedAt(Instant.now());
        center.setUpdatedAt(Instant.now());
        return center;
    }

    @Test
    void createReturnsCreated() {
        var center = aCenter();
        when(commandUseCases.create(any())).thenReturn(center);

        var request = new CreateCenterRequest(
            "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            new OperatingHours(
                new OperatingHours.DaySchedule(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                null, null, null, null, null, null, null
            ),
            100, 50, 30
        );
        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(1L);
        assertThat(response.getBody().data().name()).isEqualTo("Main Center");
    }

    @Test
    void updateReturnsOk() {
        var center = aCenter();
        when(commandUseCases.update(eq(1L), any())).thenReturn(center);

        var request = new UpdateCenterRequest(
            "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            new OperatingHours(
                new OperatingHours.DaySchedule(LocalTime.of(8, 0), LocalTime.of(17, 0)),
                null, null, null, null, null, null, null
            ),
            100, 50, 30
        );
        var response = controller.update(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(1L);
    }

    @Test
    void updateStatusReturnsOk() {
        var request = new UpdateCenterStatusRequest(CenterStatus.ACTIVE);
        var response = controller.updateStatus(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commandUseCases).updateStatus(1L, CenterStatus.ACTIVE);
    }

    @Test
    void deleteReturnsOk() {
        var response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo("Center deleted");
        verify(commandUseCases).delete(1L);
    }

    @Test
    void getByIdReturnsCenter() {
        var center = aCenter();
        when(queryUseCases.getById(1L)).thenReturn(center);

        var response = controller.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(1L);
        assertThat(response.getBody().data().name()).isEqualTo("Main Center");
    }

    @Test
    void getAllReturnsPaginatedCenters() {
        var center = aCenter();
        var result = new PageResult<DonationCenter>(List.of(center), 0, 20, 1, 1);
        when(queryUseCases.getAll(any(SearchCriteria.class))).thenReturn(result);

        var response = controller.getAll("id", "asc", 1, 20, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).hasSize(1);
        assertThat(response.getBody().page()).isNotNull();
        assertThat(response.getBody().page().number()).isEqualTo(1);
    }

    // --- ExceptionHandler tests ---

    @Test
    void notFoundReturns404() {
        var ex = new NotFoundException("Center not found: 99", "CENTER_NOT_FOUND");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("99");
    }

    @Test
    void conflictReturns409() {
        var ex = new com.zayenha.qatra._shared.exception.ConflictException(
                "Center name already exists: Dup", "CENTER_NAME_ALREADY_EXISTS");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("Dup");
    }
}
