package com.zayenha.qatra.appointment.infrastructure.web;

import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.CompleteAppointmentRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.CreateAppointmentRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.request.ScreeningRequest;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.AppointmentResponse;
import com.zayenha.qatra.appointment.infrastructure.web.dto.response.HealthScreeningResponse;
import com.zayenha.qatra.appointment.infrastructure.mapper.AppointmentMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentCommandUseCases commandUseCases;
    private final AppointmentQueryUseCases queryUseCases;
    private final AppointmentMapper mapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> book(@Valid @RequestBody CreateAppointmentRequest request) {
        var appointment = commandUseCases.book(request.donorId(), request.slotId(), request.emergencyId(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapper.toResponse(appointment)));
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> checkIn(@PathVariable Long id) {
        var appointment = commandUseCases.checkIn(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(appointment)));
    }

    @PostMapping("/{id}/screening")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<HealthScreeningResponse>> startScreening(@PathVariable Long id) {
        commandUseCases.startScreening(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> complete(
            @PathVariable Long id, @Valid @RequestBody CompleteAppointmentRequest request) {
        var outcome = mapper.toOutcome(request.outcome());
        var appointment = commandUseCases.complete(id, outcome, request.notes());
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(appointment)));
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> markNoShow(@PathVariable Long id) {
        var appointment = commandUseCases.markNoShow(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(appointment)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancel(@PathVariable Long id) {
        var appointment = commandUseCases.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(appointment)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getById(@PathVariable Long id) {
        return queryUseCases.findById(id)
                .map(a -> ResponseEntity.ok(ApiResponse.success(mapper.toResponse(a))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(null, "id", "asc", page, size);
        var result = queryUseCases.findAll(criteria);
        return ResponseEntity.ok(ApiResponse.success(
            result.content().stream().map(mapper::toResponse).toList(),
            PageHelper.fromDomain(result)
        ));
    }

    @GetMapping("/by-donor/{donorId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByDonor(@PathVariable Long donorId) {
        var appointments = queryUseCases.findByDonorId(donorId);
        return ResponseEntity.ok(ApiResponse.success(
            appointments.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/by-center/{centerId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<List<AppointmentResponse>>> getByCenterAndDate(
            @PathVariable Long centerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var appointments = queryUseCases.findByCenterIdAndDate(centerId, date);
        return ResponseEntity.ok(ApiResponse.success(
            appointments.stream().map(mapper::toResponse).toList()));
    }

    @PostMapping("/{id}/screening-results")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN', 'CENTER_STAFF')")
    public ResponseEntity<ApiResponse<HealthScreeningResponse>> saveScreening(
            @PathVariable Long id, @Valid @RequestBody ScreeningRequest request) {
        var screening = commandUseCases.saveScreening(id, request.weight(), request.bloodPressure(),
                request.hemoglobin(), request.temperature(), request.eligible(), request.notes());
        return ResponseEntity.ok(ApiResponse.success(mapper.toScreeningResponse(screening)));
    }

    @GetMapping("/{id}/screening")
    public ResponseEntity<ApiResponse<HealthScreeningResponse>> getScreening(@PathVariable Long id) {
        return queryUseCases.findScreeningByAppointmentId(id)
                .map(s -> ResponseEntity.ok(ApiResponse.success(mapper.toScreeningResponse(s))))
                .orElse(ResponseEntity.notFound().build());
    }
}
