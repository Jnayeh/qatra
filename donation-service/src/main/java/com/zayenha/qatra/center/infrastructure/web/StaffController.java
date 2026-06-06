package com.zayenha.qatra.center.infrastructure.web;

import com.zayenha.qatra.center.domain.port.in.CenterQueryUseCases;
import com.zayenha.qatra.center.infrastructure.web.dto.response.StaffProfileResponse;
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
@RequestMapping("/api/v1/staff")
public class StaffController {

    private final CenterQueryUseCases centerQueryUseCases;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CENTER_STAFF')")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> getMyProfile() {
        var userId = AuditUtils.currentUserId();
        var staff = centerQueryUseCases.getStaffByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(new StaffProfileResponse(
                staff.getId(),
                staff.getUserId(),
                staff.getCenterId(),
                staff.isVerified(),
                staff.getCreatedAt()
        )));
    }
}
