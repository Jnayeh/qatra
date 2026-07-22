package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.event.UserSignUpEvent;
import com.zayenha.qatra.user.domain.model.verification.VerificationTokenType;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.user.api.UserLoggedInEvent;
import com.zayenha.qatra.user.domain.model.*;
import com.zayenha.qatra.user.domain.model.verification.VerificationToken;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import com.zayenha.qatra.user.domain.port.out.SessionRepositoryPort;
import com.zayenha.qatra.user.domain.port.out.VerificationTokenRepositoryPort;
import com.zayenha.qatra.user.infrastructure.security.JwtTokenProvider;
import com.zayenha.qatra.user.infrastructure.security.UserDetailsAdapter;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.user.domain.exception.UserErrorCode;
import com.zayenha.qatra.user.infrastructure.web.dto.request.*;
import com.zayenha.qatra.user.infrastructure.web.dto.response.LoginResponse;
import com.zayenha.qatra.user.infrastructure.web.dto.response.VerifyEmailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserQueryUseCases userQueryUseCases;
    private final UserCommandUseCases userCommandUseCases;
    private final SessionRepositoryPort sessionRepository;
    private final VerificationTokenRepositoryPort verificationTokenRepository;
    private final EventPublisherPort eventPublisherPort;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:4200}")
    private String baseUrl;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent,
            @RequestHeader(name = "X-Forwarded-For", required = false) String ipAddress) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        log.info("User {} logged in successfully", request.email());
        var principal = (UserDetailsAdapter) auth.getPrincipal();
        var user = principal.user();
        var roles = userQueryUseCases.getUserRoles(user.getId());
        var roleNameStrings = roles.stream().map(Enum::name).collect(Collectors.toSet());

        var accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), roleNameStrings.stream().toList());
        var refreshToken = UUID.randomUUID().toString();
        var session = new Session(user.getId(), sha256(accessToken), sha256(refreshToken),
                sanitizeIp(ipAddress), userAgent, Instant.now().plus(Duration.ofDays(30)));
        sessionRepository.save(session);
        if (UserStatus.PENDING_DELETION.equals(user.getStatus())) {
            userCommandUseCases.updateStatus(user.getId(), UserStatus.ACTIVE, user.getId());
        }
        if (UserStatus.PENDING_DELETION.equals(user.getStatus()) && roleNameStrings.contains(Role.DONOR.name())) {
            applicationEventPublisher.publishEvent(new UserLoggedInEvent(this, user.getId(), user.getEmail()));
        }
        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getDisplayName(), roles, user.isEmailVerified())));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<LoginResponse>> signup(
            @Valid @RequestBody SignupRequest request,
            @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) String userAgent,
            @RequestHeader(name = "X-Forwarded-For", required = false) String ipAddress) {
        var user = userCommandUseCases.create(request.email(), request.phone(), request.password(), request.displayName(), request.firstName(), request.familyName());
        userCommandUseCases.assignRole(user.getId(), Role.DONOR, user.getId());
        var roles = userQueryUseCases.getUserRoles(user.getId());
        var roleNameStrings = roles.stream().map(Enum::name).toList();
        applicationEventPublisher.publishEvent(new UserSignUpEvent(user.getId()));

        var accessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), roleNameStrings);
        var refreshToken = UUID.randomUUID().toString();
        var session = new Session(user.getId(), sha256(accessToken), sha256(refreshToken),
                sanitizeIp(ipAddress), userAgent, Instant.now().plus(Duration.ofDays(30)));
        sessionRepository.save(session);

        sendVerificationEmail(user);

        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(accessToken, refreshToken, user.getId(), user.getEmail(), user.getDisplayName(), roles, user.isEmailVerified())));
    }

    @PostMapping("/request-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> requestVerification() {
        var userId = AuditUtils.currentUserId();
        var user = userQueryUseCases.findById(userId);
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new ValidationException("Email already verified", UserErrorCode.USER_NOT_FOUND.name());
        }
        verificationTokenRepository.findByUserIdAndType(user.getId(), VerificationTokenType.EMAIL_VERIFICATION)
                .ifPresent(t -> verificationTokenRepository.deleteById(t.getId()));
        sendVerificationEmail(user);
        return ResponseEntity.ok(ApiResponse.success(new VerifyEmailResponse("Verification email sent")));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerifyEmailResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        var tokenHash = sha256(request.token());
        var token = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new NotFoundException("Invalid or expired verification token", "INVALID_VERIFICATION_TOKEN"));

        if (!token.validate()) {
            throw new NotFoundException("Verification token expired", "VERIFICATION_TOKEN_EXPIRED");
        }

        var user = userQueryUseCases.findById(token.getUserId());

        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new ValidationException("Email already verified", UserErrorCode.USER_NOT_FOUND.name());
        }

        userCommandUseCases.verifyEmail(token.getUserId());
        token.consume();
        verificationTokenRepository.save(token);

        return ResponseEntity.ok(ApiResponse.success(new VerifyEmailResponse("Email verified successfully")));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        var token = extractToken(authHeader);
        var tokenHash = sha256(token);
        sessionRepository.findByAccessTokenHash(tokenHash).ifPresent(s -> {
            s.revoke();
            sessionRepository.save(s);
        });
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var tokenHash = sha256(request.refreshToken());
        var session = sessionRepository.findByRefreshTokenHash(tokenHash)
                .orElseThrow(() -> new NotFoundException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));

        if (!session.validate()) {
            throw new NotFoundException("Refresh token expired", "REFRESH_TOKEN_EXPIRED");
        }

        var user = userQueryUseCases.findById(session.getUserId());

        var roles = userQueryUseCases.getUserRoles(user.getId());
        var roleNameStrings = roles.stream().map(Enum::name).toList();
        var newAccessToken = tokenProvider.generateToken(user.getId(), user.getEmail(), roleNameStrings);
        var newRefreshToken = UUID.randomUUID().toString();

        session.rotateTokens(sha256(newAccessToken), sha256(newRefreshToken), Instant.now().plus(Duration.ofDays(30)));
        sessionRepository.save(session);

        return ResponseEntity.ok(ApiResponse.success(
                new LoginResponse(newAccessToken, newRefreshToken, user.getId(), user.getEmail(), user.getDisplayName(), roles, user.isEmailVerified())));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        var userId = AuditUtils.currentUserId();
        var user = userQueryUseCases.findById(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getHashedPassword())) {
            throw new ValidationException("Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }
        userCommandUseCases.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        if (!userQueryUseCases.existsByEmail(request.email())) {
            log.info("Password reset requested for unknown email — ignoring silently");
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        var user = userQueryUseCases.findByEmail(request.email());
        var rawToken = UUID.randomUUID().toString();
        var token = new VerificationToken(user.getId(), sha256(rawToken), VerificationTokenType.PASSWORD_RESET,
                Instant.now().plus(Duration.ofHours(1)));
        verificationTokenRepository.save(token);
        log.info("Password reset token saved for userId={}", user.getId());

        var resetLink = baseUrl + "/reset-password?token=" + rawToken;
        eventPublisherPort.publishPasswordReset(
                user.getId(), user.getEmail(), rawToken, resetLink);
        log.info("Password reset event published for userId={} — awaiting notification result", user.getId());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        var tokenHash = sha256(request.token());
        var token = verificationTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new NotFoundException("Invalid or expired reset token", "INVALID_RESET_TOKEN"));

        if (!token.validate()) {
            throw new NotFoundException("Reset token expired", "RESET_TOKEN_EXPIRED");
        }

        userCommandUseCases.updatePassword(token.getUserId(), passwordEncoder.encode(request.newPassword()));

        token.consume();
        verificationTokenRepository.save(token);

        sessionRepository.findActiveByUserId(token.getUserId()).ifPresent(s -> {
            s.revoke();
            sessionRepository.save(s);
        });

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private void sendVerificationEmail(User user) {
        var rawToken = UUID.randomUUID().toString();
        var token = new VerificationToken(user.getId(), sha256(rawToken), VerificationTokenType.EMAIL_VERIFICATION,
                Instant.now().plus(Duration.ofHours(24)));
        verificationTokenRepository.save(token);
        log.info("Email verification token saved for userId={}", user.getId());

        var verificationLink = baseUrl + "/verify-email?token=" + rawToken;
        eventPublisherPort.publishEmailVerification(
                user.getId(), user.getEmail(), rawToken, verificationLink);
        log.info("Email verification event published for userId={} — awaiting notification result", user.getId());
    }

    private static String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return authHeader;
    }

    private static String sanitizeIp(String ip) {
        if (ip != null && ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }
}
