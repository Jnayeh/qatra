package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.user.application.mapper.UserDomainMapper;
import com.zayenha.qatra.user.application.mapper.UserDomainMapper.UserSummary;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN')")
public class InternalUserController {

    private final UserQueryUseCases userQueryUseCases;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummary>> getUserById(@PathVariable Long id) {
        var user = userQueryUseCases.findById(id);
        return ResponseEntity.ok(ApiResponse.success(UserDomainMapper.toSummary(user)));
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserSummary>> findByEmail(@RequestParam String email) {
        var user = userQueryUseCases.findByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(UserDomainMapper.toSummary(user)));
    }

    @GetMapping("/by-phone")
    public ResponseEntity<ApiResponse<UserSummary>> findByPhone(@RequestParam String phone) {
        var user = userQueryUseCases.findByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(UserDomainMapper.toSummary(user)));
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<List<Role>>> getRoles(@PathVariable Long id) {
        var roles = userQueryUseCases.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/exists/by-email")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(@RequestParam String email) {
        return ResponseEntity.ok(ApiResponse.success(userQueryUseCases.existsByEmail(email)));
    }

    @GetMapping("/exists/by-phone")
    public ResponseEntity<ApiResponse<Boolean>> existsByPhone(@RequestParam String phone) {
        return ResponseEntity.ok(ApiResponse.success(userQueryUseCases.existsByPhone(phone)));
    }
}
