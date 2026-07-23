package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra._shared.web.PageHelper;
import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import com.zayenha.qatra.user.infrastructure.web.dto.request.*;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandUseCases commandUseCases;
    private final UserQueryUseCases queryUseCases;
    private final UserMapper mapper;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> listAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var criteria = new SearchCriteria(search, sortBy, sortDirection,
            PageHelper.toPageIndex(page), size);
        var result = queryUseCases.findAll(criteria);
        var users = result.content().stream()
                .map(mapper::toDetail)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users, PageHelper.fromDomain(result)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetails(@PathVariable Long id) {
        var user = queryUseCases.findById(id);
        return ResponseEntity.ok(ApiResponse.success(mapper.toDetail(user)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        var user = commandUseCases.create(
                request.email(), request.phone(), request.password(), request.displayName(), request.firstName(), request.familyName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(mapper.toDetail(user)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDetailResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        var user = commandUseCases.update(
                id, request.email(), request.phone(), request.displayName(), request.firstName(), request.familyName());
        return ResponseEntity.ok(ApiResponse.success(mapper.toDetail(user)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        commandUseCases.updateStatus(id, request.status(), AuditUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        commandUseCases.assignRole(id, request.role(), AuditUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> revokeRole(
            @PathVariable Long id,
            @RequestParam Role role) {
        commandUseCases.revokeRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("Role revoked"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        commandUseCases.delete(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}
