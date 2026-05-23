package com.zayenha.qatra.system.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.system.domain.port.in.GDPRCommandUseCases;
import com.zayenha.qatra.system.domain.port.in.GDPRQueryUseCases;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRRequestDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.response.GDPRDeletionResponse;
import com.zayenha.qatra.system.infrastructure.mapper.SystemMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final GDPRCommandUseCases gdprCommandUseCases;
    private final GDPRQueryUseCases gdprQueryUseCases;
    private final SystemMapper mapper;

    @PostMapping("/gdpr/request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'DONOR')")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> requestDeletion(@Valid @RequestBody GDPRRequestDeletionRequest request) {
        var gdpr = gdprCommandUseCases.requestDeletion(request.userId(), request.reason());
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(gdpr)));
    }

    @PostMapping("/gdpr/{id}/complete")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> completeDeletion(@PathVariable Long id) {
        var gdpr = gdprCommandUseCases.complete(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(gdpr)));
    }

    @PostMapping("/gdpr/{id}/cancel")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> cancelDeletion(@PathVariable Long id) {
        var gdpr = gdprCommandUseCases.cancel(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(gdpr)));
    }

    @GetMapping("/gdpr")
    public ResponseEntity<ApiResponse<List<GDPRDeletionResponse>>> getAllDeletionRequests() {
        var requests = gdprQueryUseCases.findAll();
        return ResponseEntity.ok(ApiResponse.success(
            requests.stream().map(mapper::toResponse).toList()));
    }

    @GetMapping("/gdpr/{id}")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> getDeletionRequest(@PathVariable Long id) {
        var request = gdprQueryUseCases.findById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(request)));
    }
}
