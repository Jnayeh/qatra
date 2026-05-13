package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.infrastructure.mapper.CenterMapper;
import com.zayenha.qatra.center.infrastructure.web.dto.request.*;
import com.zayenha.qatra.center.infrastructure.web.dto.response.*;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterControllerTest {

    @Mock
    private CenterCommandUseCases commandUseCases;
    @Mock
    private CenterQueryUseCases queryUseCases;
    @Mock
    private CenterMapper mapper;

    private CenterController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new CenterController(commandUseCases, queryUseCases, mapper);
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
                100, 50, 30, 0L);
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
        when(mapper.toResponse(center)).thenReturn(new CenterResponse(
            1L, "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            center.getOperatingHours(),
            CenterStatus.ACTIVE, 100, 50, 30,
            center.getCreatedAt(), center.getUpdatedAt()
        ));

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
        when(mapper.toResponse(center)).thenReturn(new CenterResponse(
            1L, "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            center.getOperatingHours(),
            CenterStatus.ACTIVE, 100, 50, 30,
            center.getCreatedAt(), center.getUpdatedAt()
        ));

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
        when(queryUseCases.getById(1L, false)).thenReturn(center);
        when(mapper.toResponse(center)).thenReturn(new CenterResponse(
            1L, "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            center.getOperatingHours(),
            CenterStatus.ACTIVE, 100, 50, 30,
            center.getCreatedAt(), center.getUpdatedAt()
        ));

        var response = controller.getById(1L, false);

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

    @Test
    void addClosureReturnsClosureResponse() {
        var result = new CenterCommandUseCases.ClosureResult(3, LocalDate.of(2025, 8, 15), "Holiday");
        when(commandUseCases.addClosure(eq(1L), any())).thenReturn(result);

        var request = new CreateClosureRequest(LocalDate.of(2025, 8, 15), "08:00", "17:00", false, "Holiday");
        var response = controller.addClosure(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().blockedSlotCount()).isEqualTo(3);
        assertThat(response.getBody().data().reason()).isEqualTo("Holiday");
    }

    @Test
    void blockSlotReturnsSlotResponse() {
        var slot = new Slot(1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 5);
        slot.setId(100L);
        slot.setBlocked(true);
        when(commandUseCases.blockSlot(1L, 100L, true)).thenReturn(slot);
        when(mapper.toSlotResponse(slot)).thenReturn(new SlotResponse(
            slot.getId(), slot.getCenterId(), slot.getDate(),
            slot.getStartTime(), slot.getEndTime(),
            slot.getMaxBookings(), slot.getMaxRegularBookings(),
            slot.getBookedCount(), slot.getRegularBookedCount(), slot.isBlocked()
        ));

        var request = new BlockSlotRequest(true);
        var response = controller.blockSlot(1L, 100L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().isBlocked()).isTrue();
    }

    @Test
    void getSlotsReturnsSlotList() {
        var slot = new Slot(1L, LocalDate.now(), LocalTime.of(9, 0), LocalTime.of(10, 0), 10, 5);
        slot.setId(100L);
        when(queryUseCases.getSlots(eq(1L), any(), any(), eq(false))).thenReturn(List.of(slot));

        var response = controller.getSlots(1L, null, null, false);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void addStaffReturnsCreated() {
        var staff = new CenterStaffProfile(10L, 1L);
        staff.setId(100L);
        when(commandUseCases.addStaff(1L, 10L)).thenReturn(staff);
        when(mapper.toStaffResponse(staff)).thenReturn(new StaffSummaryResponse(
            staff.getId(), staff.getUserId(), staff.getCenterId(),
            staff.isVerified(), staff.getCreatedAt()
        ));

        var request = new AddStaffRequest(10L);
        var response = controller.addStaff(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().userId()).isEqualTo(10L);
    }

    @Test
    void removeStaffReturnsOk() {
        var response = controller.removeStaff(1L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo("Staff removed");
        verify(commandUseCases).removeStaff(1L, 10L);
    }

    @Test
    void getPendingReturnsPaginatedCenters() {
        var center = aCenter();
        var result = new PageResult<DonationCenter>(List.of(center), 0, 20, 1, 1);
        when(queryUseCases.getPending(any(SearchCriteria.class))).thenReturn(result);

        var response = controller.getPending("id", "asc", 1, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void approveReturnsCenter() {
        var center = aCenter();
        center.setStatus(CenterStatus.ACTIVE);
        when(commandUseCases.approve(1L, true, "Approved")).thenReturn(center);
        when(mapper.toResponse(center)).thenReturn(new CenterResponse(
            1L, "Main Center", "123 Street", "City", "Country",
            "12345", "1234567890", "center@test.com",
            40.7128, -74.0060, FacilityType.BLOOD_BANK,
            center.getOperatingHours(),
            CenterStatus.ACTIVE, 100, 50, 30,
            center.getCreatedAt(), center.getUpdatedAt()
        ));

        var request = new ApproveCenterRequest(true, "Approved");
        var response = controller.approve(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().status()).isEqualTo(CenterStatus.ACTIVE);
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
