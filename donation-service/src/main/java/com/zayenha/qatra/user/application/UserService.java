package com.zayenha.qatra.user.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra._shared.event.AuditUtils;
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
import com.zayenha.qatra.user.domain.port.out.PasswordEncoderPort;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.domain.service.UserDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserCommandUseCases, UserQueryUseCases {

    private final UserRepositoryPort userRepository;
    private final UserRoleRepositoryPort userRoleRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${super-admin.email:}")
    private String superAdminEmail;

    @Value("${super-admin.phone:}")
    private String superAdminPhone;

    @Value("${super-admin.password:}")
    private String superAdminPassword;

    private UserDomainValidator validator() {
        return new UserDomainValidator(userRepository);
    }

    private void audit(String action, Long entityId, String oldValue, String newValue) {
        eventPublisher.publishEvent(new AuditEvent(AuditUtils.currentUserId(), action, "User", entityId, oldValue, newValue, null, null));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userExists"}, allEntries = true)
    public User create(String email, String phone, String password, String displayName) {
        validator().validateCreate(email, phone);
        var user = new User(email, phone, passwordEncoder.encode(password), displayName);
        user = userRepository.save(user);
        eventPublisher.publishEvent(
                new UserCreatedEvent(this, user.getId(), user.getEmail())
        );
        audit("USER_CREATED", user.getId(), null, "email=" + email);
        return user;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userExists"}, allEntries = true)
    public User update(Long id, String email, String phone, String displayName) {
        validator().validateUpdate(id, email, phone);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldEmail = user.getEmail();
        log.info("validated");
        user.update(email, phone, displayName);
        user = userRepository.save(user); //UP
        audit("USER_UPDATED", id, "email=" + oldEmail, "email=" + email);
        return user;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users"}, allEntries = true)
    public void updateStatus(Long id, UserStatus status) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldStatus = user.getStatus();
        user.updateStatus(status);
        userRepository.save(user);
        audit("USER_STATUS_CHANGED", id, "oldStatus=" + oldStatus, "newStatus=" + status);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userRoles"}, allEntries = true)
    public void assignRole(Long userId, Role role) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!user.isActive()) {
            throw new InvalidRoleAssignmentException("Cannot assign roles to inactive user");
        }
        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new InvalidRoleAssignmentException("User already has role: " + role);
        }

        userRoleRepository.save(new UserRole(userId, role));
        audit("ROLE_ASSIGNED", userId, null, "role=" + role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userRoles"}, allEntries = true)
    public void revokeRole(Long userId, Role role) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        if (!userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new InvalidRoleAssignmentException("User does not have role: " + role);
        }
        userRoleRepository.deleteByUserIdAndRole(userId, role);
        audit("ROLE_REVOKED", userId, null, "role=" + role);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"users", "userExists", "userRoles"}, allEntries = true)
    public void delete(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (user.isActive()) {
            throw new CannotDeleteActiveUserException(id);
        }
        user.markDeleted();
        userRepository.save(user);
        userRoleRepository.deleteByUserId(id);
        audit("USER_DELETED", id, null, "");
    }

    @Override
    @Transactional
    // ponytail: null-safe for test-created instances bypassing @Value
    public void seedSuperAdminIfAbsent() {
        if (superAdminEmail == null || superAdminEmail.isBlank()
                || superAdminPhone == null || superAdminPhone.isBlank()
                || superAdminPassword == null || superAdminPassword.isBlank()) return;
        if (userRepository.existsByEmail(superAdminEmail)) {
            return;
        }
        var user = create(superAdminEmail, superAdminPhone, superAdminPassword, "Super Admin");
        userRoleRepository.save(new UserRole(user.getId(), Role.SUPER_ADMIN));
    }

    @Override
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    @Cacheable(value = "users", key = "'email:' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Cacheable(value = "users", key = "'phone:' + #phone")
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    @Cacheable(value = "userExists", key = "'email:' + #email")
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Cacheable(value = "userExists", key = "'phone:' + #phone")
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    @Cacheable(value = "userRoles", key = "#userId")
    public List<Role> getUserRoles(Long userId) {
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRole)
                .toList();
    }

    @Override
    public PageResult<User> findAll(SearchCriteria criteria) {
        return userRepository.findAll(criteria);
    }
}
