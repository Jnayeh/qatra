package com.zayenha.qatra.analytics.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.analytics.infrastructure.web.dto.response.SystemConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/")
public class SystemAdminController {

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<SystemConfigResponse>>> getConfig() {
        var configs = List.of(
            new SystemConfigResponse("emergency.expiration.minutes", 180, "Minutes before an emergency request expires"),
            new SystemConfigResponse("emergency.escalation.radius.increment", 10, "Km to expand radius on escalation"),
            new SystemConfigResponse("eligibility.cooldown.days", 56, "Days before a donor can donate again"),
            new SystemConfigResponse("slot.generation.lookahead.days", 21, "Days ahead to auto-generate slots"),
            new SystemConfigResponse("slot.default.period.minutes", 60, "Default slot duration in minutes"),
            new SystemConfigResponse("jwt.expiration.ms", 86400000, "JWT token expiration in milliseconds"),
            new SystemConfigResponse("notification.emergency.channels", "IN_APP", "Channels for emergency notifications"),
            new SystemConfigResponse("notification.appointment.channels", "IN_APP", "Channels for appointment notifications"),
            new SystemConfigResponse("notification.eligibility.channels", "IN_APP,EMAIL", "Channels for eligibility notifications")
        );
        return ResponseEntity.ok(ApiResponse.success(configs));
    }
}