package com.zayenha.qatra.emergency.infrastructure.web;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.AcceptResponseRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.CreateEmergencyRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.request.UpdateEmergencyRequest;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.DonorResponseResponse;
import com.zayenha.qatra.emergency.infrastructure.web.dto.response.EmergencyResponse;
import com.zayenha.qatra.emergency.infrastructure.web.mapper.EmergencyMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyCommandUseCases commandUseCases;
    private final EmergencyQueryUseCases queryUseCases;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponse>> create(@Valid @RequestBody CreateEmergencyRequest request) {
        var emergency = commandUseCases.create(
            request.patientName(), request.bloodType(), request.unitsNeeded(),
            request.urgency(), request.hospital(), request.latitude(), request.longitude(),
            request.contactPhone());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EmergencyMapper.toResponse(emergency)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateEmergencyRequest request) {
        var emergency = commandUseCases.update(
            id, request.patientName(), request.bloodType(), request.unitsNeeded(),
            request.urgency(), request.hospital(), request.latitude(), request.longitude(),
            request.contactPhone());
        return ResponseEntity.ok(ApiResponse.success(EmergencyMapper.toResponse(emergency)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<EmergencyResponse>> cancel(@PathVariable Long id) {
        var emergency = commandUseCases.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(EmergencyMapper.toResponse(emergency)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<EmergencyResponse>> getById(@PathVariable Long id) {
        return queryUseCases.findById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.success(EmergencyMapper.toResponse(e))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmergencyResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(null, "id", "asc", page, size);
        var result = queryUseCases.findAll(criteria);
        var emergencies = result.content().stream().map(EmergencyMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(emergencies, PageHelper.fromDomain(result)));
    }

    @GetMapping("/open/{bloodType}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmergencyResponse>>> getOpenByBloodType(@PathVariable BloodType bloodType) {
        var emergencies = queryUseCases.findOpenByBloodType(bloodType);
        return ResponseEntity.ok(ApiResponse.success(
            emergencies.stream().map(EmergencyMapper::toResponse).toList()));
    }

    @GetMapping("/nearby")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmergencyResponse>>> getNearby(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "50") double radiusKm) {
        var emergencies = queryUseCases.findOpenWithinRadius(latitude, longitude, radiusKm);
        return ResponseEntity.ok(ApiResponse.success(
            emergencies.stream().map(EmergencyMapper::toResponse).toList()));
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<DonorResponseResponse>> respond(
            @PathVariable Long id) {
        var donorId = AuditUtils.currentUserId(); // ponytail: SUPER_ADMIN cannot respond on behalf of others
        var response = commandUseCases.respond(id, donorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(EmergencyMapper.toResponse(response)));
    }

    @PostMapping("/responses/{responseId}/accept")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<DonorResponseResponse>> acceptResponse(
            @PathVariable Long responseId, @Valid @RequestBody AcceptResponseRequest request) {
        var response = commandUseCases.acceptResponse(responseId, request.slotId());
        return ResponseEntity.ok(ApiResponse.success(EmergencyMapper.toResponse(response)));
    }

    @PostMapping("/responses/{responseId}/decline")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<DonorResponseResponse>> declineResponse(@PathVariable Long responseId) {
        var response = commandUseCases.declineResponse(responseId);
        return ResponseEntity.ok(ApiResponse.success(EmergencyMapper.toResponse(response)));
    }

    @GetMapping("/{id}/responses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DonorResponseResponse>>> getResponses(@PathVariable Long id) {
        var responses = queryUseCases.findResponsesByEmergencyId(id);
        return ResponseEntity.ok(ApiResponse.success(
            responses.stream().map(EmergencyMapper::toResponse).toList()));
    }

    @GetMapping("/responses/donor/{donorId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DonorResponseResponse>>> getDonorResponses(@PathVariable Long donorId) {
        var responses = queryUseCases.findResponsesByDonorId(donorId);
        return ResponseEntity.ok(ApiResponse.success(
            responses.stream().map(EmergencyMapper::toResponse).toList()));
    }
}
