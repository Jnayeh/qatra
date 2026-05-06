package com.zayenha.qatra.user.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra.user.api.dto.UserCreatedEvent;
import com.zayenha.qatra.user.domain.exception.CannotDeleteActiveUserException;
import com.zayenha.qatra.user.domain.exception.InvalidRoleAssignmentException;
import com.zayenha.qatra.user.domain.exception.UserNotFoundException;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserRole;
import com.zayenha.qatra.user.domain.model.UserStatus;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.domain.service.UserDomainValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserCommandUseCases, UserQueryUseCases {

    private final UserRepositoryPort userRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final CacheService cacheService;
    private final AuditPublisher auditPublisher;

    @Value("${super-admin.email:}")
    private String superAdminEmail;

    @Value("${super-admin.phone:}")
    private String superAdminPhone;

    @Value("${super-admin.password:}")
    private String superAdminPassword;

    private UserDomainValidator validator() {
        return new UserDomainValidator(userRepository);
    }

    @Override
    @Transactional
    public User create(String email, String phone, String password, String displayName) {
        validator().validateCreate(email, phone);
        var user = new User(email, phone, passwordEncoder.encode(password), displayName);
        user = userRepository.save(user);
        cacheService.evictByPattern("users:*");
        cacheService.evictByPattern("userExists:*");
        eventPublisher.publishEvent(
                new UserCreatedEvent(this, user.getId(), user.getEmail())
        );
        auditPublisher.publish("USER_CREATED", user.getId(), "User", null, Map.of("email", email, "phone", phone));
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, String email, String phone, String displayName) {
        validator().validateUpdate(id, email, phone);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldEmail = user.getEmail();
        log.info("validated");
        user.update(email, phone, displayName);
        user = userRepository.save(user);
        cacheService.evictByPattern("users:*");
        cacheService.evictByPattern("userExists:*");
        auditPublisher.publish("USER_UPDATED", id, "User",
            Map.of("email", oldEmail),
            Map.of("email", email, "phone", phone));
        return user;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatus status) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldStatus = user.getStatus();
        user.updateStatus(status);
        userRepository.save(user);
        cacheService.evictByPattern("users:*");
        auditPublisher.publish("USER_STATUS_CHANGED", id, "User",
            Map.of("status", oldStatus.name()),
            Map.of("status", status.name()));
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String newEncodedPassword) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.changePassword(newEncodedPassword);
        userRepository.save(user);
        cacheService.evictByPattern("users:*");
        auditPublisher.publish("USER_PASSWORD_CHANGED", userId, "User", null, null);
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Role role) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.isActive()) {
            throw new InvalidRoleAssignmentException("Cannot assign roles to inactive user");
        }
        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new InvalidRoleAssignmentException("User already has role: " + role);
        }

        userRoleRepository.save(new UserRole(userId, role));
        cacheService.evictByPattern("userRoles:*");
        auditPublisher.publish("ROLE_ASSIGNED", userId, "User", null, Map.of("role", role.name(), "userId", userId));
    }

    @Override
    @Transactional
    public void revokeRole(Long userId, Role role) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        if (!userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new InvalidRoleAssignmentException("User does not have role: " + role);
        }
        userRoleRepository.deleteByUserIdAndRole(userId, role);
        cacheService.evictByPattern("userRoles:*");
        auditPublisher.publish("ROLE_REVOKED", userId, "User", Map.of("role", role.name()), null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (user.isActive()) {
            throw new CannotDeleteActiveUserException(id);
        }
        user.markDeleted();
        userRepository.save(user);
        userRoleRepository.deleteByUserId(id);
        cacheService.evictByPattern("users:*");
        cacheService.evictByPattern("userExists:*");
        cacheService.evictByPattern("userRoles:*");
        auditPublisher.publish("USER_DELETED", id, "User", Map.of("email", user.getEmail()), null);
    }

    @Override
    @Transactional
    // ponytail: null-safe for test-created instances bypassing @Value
    public void seedSuperAdminIfAbsent() {
        if (superAdminEmail == null || superAdminEmail.isBlank()
                || superAdminPhone == null || superAdminPhone.isBlank()
                || superAdminPassword == null || superAdminPassword.isBlank()) return;
        if (userRepository.existsByEmail(superAdminEmail)) return;
        var user = create(superAdminEmail, superAdminPhone, superAdminPassword, "Super Admin");
        userRoleRepository.save(new UserRole(user.getId(), Role.SUPER_ADMIN));
    }

    @Override
    public Optional<User> findById(Long id) {
        var key = "users:" + id;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached;
        var result = userRepository.findById(id);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        var key = "users:email:" + email;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached;
        var result = userRepository.findByEmail(email);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        var key = "users:phone:" + phone;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached;
        var result = userRepository.findByPhone(phone);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    @Override
    public boolean existsByEmail(String email) {
        var key = "userExists:email:" + email;
        var cached = cacheService.get(key, Boolean.class);
        if (cached.isPresent()) return cached.get();
        var result = userRepository.existsByEmail(email);
        cacheService.put(key, result);
        return result;
    }

    @Override
    public boolean existsByPhone(String phone) {
        var key = "userExists:phone:" + phone;
        var cached = cacheService.get(key, Boolean.class);
        if (cached.isPresent()) return cached.get();
        var result = userRepository.existsByPhone(phone);
        cacheService.put(key, result);
        return result;
    }

    @Override
    public List<Role> getUserRoles(Long userId) {
        var key = "userRoles:" + userId;
        var cached = cacheService.get(key, new TypeReference<List<Role>>() {});
        if (cached.isPresent()) return cached.get();
        var result = userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRole)
                .toList();
        cacheService.put(key, result);
        return result;
    }

    @Override
    public PageResult<User> findAll(SearchCriteria criteria) {
        return userRepository.findAll(criteria);
    }
}
