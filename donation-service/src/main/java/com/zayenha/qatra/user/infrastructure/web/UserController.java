package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra.shared.web.ApiResponse;
import com.zayenha.qatra.shared.web.Page;
import com.zayenha.qatra.user.domain.model.UserSearchCriteria;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import com.zayenha.qatra.user.infrastructure.web.dto.request.*;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import com.zayenha.qatra.user.infrastructure.web.mapper.UserResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandUseCases commandUseCases;
    private final UserQueryUseCases queryUseCases;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDetailResponse>>> listAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var safePage = page > 0 ? page - 1 : 0;
        var criteria = new UserSearchCriteria(search, sortBy, sortDirection, safePage, size);
        var result = queryUseCases.findAll(criteria);
        var users = result.content().stream()
                .map(u -> UserResponseMapper.toDetail(u, queryUseCases.getUserRoles(u.getId())))
                .toList();
        var paging = new Page(result.page() + 1 , result.size(),
            result.totalElements(), result.totalPages());
        return ResponseEntity.ok(ApiResponse.success(users, paging));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getDetails(@PathVariable Long id) {
        var user = queryUseCases.findById(id)
                .orElseThrow(() -> new com.zayenha.qatra.shared.exception.NotFoundException("User", id));
        var roles = queryUseCases.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(UserResponseMapper.toDetail(user, roles)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDetailResponse>> create(
            @Valid @RequestBody CreateUserRequest request) {
        var user = commandUseCases.create(
                request.email(), request.phone(), request.password(), request.displayName());
        var roles = queryUseCases.getUserRoles(user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(UserResponseMapper.toDetail(user, roles)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        var user = commandUseCases.update(
                id, request.email(), request.phone(), request.displayName());
        var roles = queryUseCases.getUserRoles(id);
        return ResponseEntity.ok(ApiResponse.success(UserResponseMapper.toDetail(user, roles)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateUserStatusRequest request) {
        commandUseCases.updateStatus(id, request.status());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable Long id, @Valid @RequestBody AssignRoleRequest request) {
        commandUseCases.assignRole(id, request.role());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<Void>> revokeRole(
            @PathVariable Long id, @Valid @RequestBody RevokeRoleRequest request) {
        commandUseCases.revokeRole(id, request.role());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        commandUseCases.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
