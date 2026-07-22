package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases;
import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.infrastructure.web.dto.request.*;
import com.zayenha.qatra.center.infrastructure.web.dto.response.*;
import com.zayenha.qatra.center.infrastructure.mapper.CenterMapper;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/centers")
@RequiredArgsConstructor
public class CenterController {

    private final CenterCommandUseCases commandUseCases;
    private final CenterQueryUseCases queryUseCases;
    private final CenterMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<CenterResponse>> create(@Valid @RequestBody CreateCenterRequest request) {
        var command = new CenterCommandUseCases.CreateCenterCommand(
            request.name(), request.address(), request.city(), request.country(),
            request.postalCode(), request.phone(), request.email(),
            request.latitude(), request.longitude(), request.facilityType(),
            request.operatingHours(), request.totalCapacity(), request.maxRegular(), request.slotPeriod());
        var center = commandUseCases.create(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toResponse(center)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<CenterResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateCenterRequest request) {
        var command = new CenterCommandUseCases.UpdateCenterCommand(
            request.name(), request.address(), request.city(), request.country(),
            request.postalCode(), request.phone(), request.email(),
            request.latitude(), request.longitude(), request.facilityType(),
            request.operatingHours(), request.totalCapacity(), request.maxRegular(), request.slotPeriod());
        var center = commandUseCases.update(id, command);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(center)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateCenterStatusRequest request) {
        commandUseCases.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        commandUseCases.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Center deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CenterResponse>> getById(@PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean fetchJoins) {
        var center = queryUseCases.getById(id, fetchJoins);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(center)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CenterResponse>>> getAll(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        var criteria = new SearchCriteria(search, sortBy, sortDirection, PageHelper.toPageIndex(page), size);
        var result = queryUseCases.getAll(criteria);
        var centers = result.content().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(centers, PageHelper.fromDomain(result)));
    }

    @PostMapping("/{id}/closures")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<ClosureResponse>> addClosure(@PathVariable Long id,
            @Valid @RequestBody CreateClosureRequest request) {
        var command = new CenterCommandUseCases.ClosureCommand(
            request.date(), request.startTime(), request.endTime(), request.allDay(), request.reason());
        var result = commandUseCases.addClosure(id, command);
        return ResponseEntity.ok(ApiResponse.success(new ClosureResponse(result.blockedSlotCount(), result.date(), result.reason())));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<ApiResponse<List<SlotResponse>>> getSlots(@PathVariable Long id,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String slotType,
            @RequestParam(defaultValue = "false") boolean fetchJoins) {
        var slots = queryUseCases.getSlots(id, date, slotType, fetchJoins);
        return ResponseEntity.ok(ApiResponse.success(slots.stream().map(mapper::toSlotResponse).toList()));
    }

    @PatchMapping("/{id}/slots/{slotId}/block")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<SlotResponse>> blockSlot(@PathVariable Long id, @PathVariable Long slotId,
            @Valid @RequestBody BlockSlotRequest request) {
        var slot = commandUseCases.blockSlot(id, slotId, request.isBlocked());
        return ResponseEntity.ok(ApiResponse.success(mapper.toSlotResponse(slot)));
    }

    @GetMapping("/{id}/staff")
    public ResponseEntity<ApiResponse<List<StaffSummaryResponse>>> getStaff(@PathVariable Long id) {
        var staff = queryUseCases.getStaff(id);
        return ResponseEntity.ok(ApiResponse.success(staff.stream().map(mapper::toStaffResponse).toList()));
    }

    @PostMapping("/{id}/staff")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<StaffSummaryResponse>> addStaff(@PathVariable Long id,
            @Valid @RequestBody AddStaffRequest request) {
        var staff = commandUseCases.addStaff(id, request.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapper.toStaffResponse(staff)));
    }

    @DeleteMapping("/{id}/staff/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> removeStaff(@PathVariable Long id, @PathVariable Long userId) {
        commandUseCases.removeStaff(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Staff removed"));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<CenterResponse>>> getPublic(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {
        var centers = queryUseCases.getAllActive(lat, lng);
        return ResponseEntity.ok(ApiResponse.success(centers.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<CenterResponse>>> getPending(
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(null, sortBy, sortDirection, PageHelper.toPageIndex(page), size);
        var result = queryUseCases.getPending(criteria);
        var centers = result.content().stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(centers, PageHelper.fromDomain(result)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<CenterResponse>> approve(@PathVariable Long id,
            @Valid @RequestBody ApproveCenterRequest request) {
        var center = commandUseCases.approve(id, request.approved(), request.reason());
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(center)));
    }
}
