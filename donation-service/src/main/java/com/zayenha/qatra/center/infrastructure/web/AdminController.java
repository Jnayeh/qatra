package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.infrastructure.mapper.CenterMapper;
import com.zayenha.qatra.center.infrastructure.web.dto.response.CenterAdminDTO;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/")
public class AdminController {

    private final CenterQueryUseCases centerQueryUseCases;
    private final CenterMapper mapper;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<CenterAdminDTO>> getMyProfile() {
        var userId = AuditUtils.currentUserId();
        var admin = centerQueryUseCases.getAdminByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(mapper.toAdminDTO(admin)));
    }
}
