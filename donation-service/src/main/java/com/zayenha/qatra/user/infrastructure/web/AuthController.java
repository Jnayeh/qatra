package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.infrastructure.security.JwtTokenProvider;
import com.zayenha.qatra.user.infrastructure.security.UserDetailsAdapter;
import com.zayenha.qatra.user.infrastructure.web.dto.request.LoginRequest;
import com.zayenha.qatra.user.infrastructure.web.dto.response.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRoleRepositoryPort userRoleRepository;
    private final UserRepositoryPort userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        var principal = (UserDetailsAdapter) auth.getPrincipal();
        var user = principal.user();
        var roles = userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole())
                .toList();

        var roleNames = roles.stream().map(Enum::name).toList();
        var token = tokenProvider.generateToken(user.getId(), user.getEmail(), roleNames);

        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(token, user.getId(), user.getEmail(), user.getDisplayName(), roles)));
    }
}
