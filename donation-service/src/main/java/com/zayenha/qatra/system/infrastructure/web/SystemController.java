package com.zayenha.qatra.system.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.system.application.FeatureFlagService;
import com.zayenha.qatra.system.application.GDPRService;
import com.zayenha.qatra.system.application.SystemConfigService;
import com.zayenha.qatra.system.infrastructure.web.dto.request.CreateFeatureFlagRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRActionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.GDPRRequestDeletionRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.request.SetConfigRequest;
import com.zayenha.qatra.system.infrastructure.web.dto.response.FeatureFlagResponse;
import com.zayenha.qatra.system.infrastructure.web.dto.response.GDPRDeletionResponse;
import com.zayenha.qatra.system.infrastructure.web.dto.response.SystemConfigResponse;
import com.zayenha.qatra.system.infrastructure.web.mapper.SystemMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
public class SystemController {

    private final SystemConfigService configService;
    private final FeatureFlagService flagService;
    private final GDPRService gdprService;

    @GetMapping("/config")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getAllConfig() {
        var configs = configService.getAll();
        return ResponseEntity.ok(ApiResponse.success(
            configs.stream().map(SystemMapper::toResponse).toList()));
    }

    @GetMapping("/config/{key}")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> getConfig(@PathVariable String key) {
        return configService.get(key)
                .map(c -> ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(c))))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/config")
    public ResponseEntity<ApiResponse<SystemConfigResponse>> setConfig(@Valid @RequestBody SetConfigRequest request) {
        var config = configService.set(request.key(), request.value(), request.description());
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(config)));
    }

    @DeleteMapping("/config/{key}")
    public ResponseEntity<ApiResponse<String>> deleteConfig(@PathVariable String key) {
        configService.delete(key);
        return ResponseEntity.ok(ApiResponse.success("Config deleted"));
    }

    @GetMapping("/feature-flags")
    public ResponseEntity<ApiResponse<List<FeatureFlagResponse>>> getAllFlags() {
        var flags = flagService.getAll();
        return ResponseEntity.ok(ApiResponse.success(
            flags.stream().map(SystemMapper::toResponse).toList()));
    }

    @PostMapping("/feature-flags")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> createFlag(@Valid @RequestBody CreateFeatureFlagRequest request) {
        var flag = flagService.create(request.flagName(), request.enabled(), request.description());
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(flag)));
    }

    @PostMapping("/feature-flags/{flagName}/enable")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> enableFlag(@PathVariable String flagName) {
        var flag = flagService.enable(flagName);
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(flag)));
    }

    @PostMapping("/feature-flags/{flagName}/disable")
    public ResponseEntity<ApiResponse<FeatureFlagResponse>> disableFlag(@PathVariable String flagName) {
        var flag = flagService.disable(flagName);
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(flag)));
    }

    @GetMapping("/feature-flags/{flagName}")
    public ResponseEntity<ApiResponse<Boolean>> isFlagEnabled(@PathVariable String flagName) {
        return ResponseEntity.ok(ApiResponse.success(flagService.isEnabled(flagName)));
    }

    @PostMapping("/gdpr/request")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> requestDeletion(@Valid @RequestBody GDPRRequestDeletionRequest request) {
        var gdpr = gdprService.requestDeletion(request.userId(), request.reason());
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(gdpr)));
    }

    @PostMapping("/gdpr/{id}/approve")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> approveDeletion(
            @PathVariable Long id, @RequestBody GDPRActionRequest request) {
        var gdpr = gdprService.approve(id, request.processedBy());
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(gdpr)));
    }

    @PostMapping("/gdpr/{id}/reject")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> rejectDeletion(
            @PathVariable Long id, @RequestBody GDPRActionRequest request) {
        var gdpr = gdprService.reject(id, request.processedBy());
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(gdpr)));
    }

    @PostMapping("/gdpr/{id}/complete")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> completeDeletion(@PathVariable Long id) {
        var gdpr = gdprService.complete(id);
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(gdpr)));
    }

    @GetMapping("/gdpr")
    public ResponseEntity<ApiResponse<List<GDPRDeletionResponse>>> getAllDeletionRequests() {
        var requests = gdprService.findAll();
        return ResponseEntity.ok(ApiResponse.success(
            requests.stream().map(SystemMapper::toResponse).toList()));
    }

    @GetMapping("/gdpr/{id}")
    public ResponseEntity<ApiResponse<GDPRDeletionResponse>> getDeletionRequest(@PathVariable Long id) {
        var request = gdprService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(SystemMapper.toResponse(request)));
    }
}
