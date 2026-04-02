package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra.user.api.dto.UserSummary;
import com.zayenha.qatra.user.domain.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
public class InternalUserController {

    private final UserApi userApi;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummary>> findById(@PathVariable Long id) {
        return userApi.findById(id)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserSummary>> findByEmail(@RequestParam String email) {
        return userApi.findByEmail(email)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-phone")
    public ResponseEntity<ApiResponse<UserSummary>> findByPhone(@RequestParam String phone) {
        return userApi.findByPhone(phone)
                .map(u -> ResponseEntity.ok(ApiResponse.success(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles(@PathVariable Long id) {
        var roles = userApi.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/exists/by-email")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(userApi.existsByEmail(email)));
    }

    @GetMapping("/exists/by-phone")
    public ResponseEntity<ApiResponse<Boolean>> existsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.success(userApi.existsByPhone(phone)));
    }
}
