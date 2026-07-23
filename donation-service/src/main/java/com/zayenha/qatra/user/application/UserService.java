package com.zayenha.qatra.user.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.AuthorizationException;
import com.zayenha.qatra.user.domain.exception.CannotDeleteUserException;
import com.zayenha.qatra.user.domain.exception.InvalidRoleAssignmentException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.user.domain.exception.UserErrorCode;
import com.zayenha.qatra.user.domain.exception.UserNotFoundException;
import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserRole;
import com.zayenha.qatra._shared.domain.UserStatus;
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


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserCommandUseCases, UserQueryUseCases {

    public static final String USER_ROLES = "userRoles:*";
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
    public User create(String email, String phone, String password, String displayName, String firstName, String lastName) {
        validator().validateCreate(email, phone);
        var user = new User(email, phone, passwordEncoder.encode(password), displayName, firstName, lastName);
        user = userRepository.save(user);
        cacheService.evictByPattern("users:*");
        cacheService.evictByPattern("userExists:*");
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, String email, String phone, String displayName, String firstName, String familyName) {
        if (!AuditUtils.currentUserId().equals(id)
            && !userRoleRepository.existsByUserIdAndRole(AuditUtils.currentUserId(), Role.SUPER_ADMIN)) {
           throw  new AuthorizationException("You can not modify this user", "ACCESS_DENIED");
        }
        validator().validateUpdate(id, email, phone);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldEmail = user.getEmail();
        log.info("validated");
        user.update(email, phone, displayName, firstName, familyName);
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
    public void updateStatus(Long id, UserStatus status, Long actorID) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        var oldStatus = user.getStatus();
        user.updateStatus(status);
        userRepository.save(user);
        cacheService.evictByPattern("users:*");
        auditPublisher.publish(actorID, "USER_STATUS_CHANGED", id, "User",
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
        auditPublisher.publish(userId, "USER_PASSWORD_CHANGED", userId, "User", null, null);
    }

    @Override
    @Transactional
    public void requestDeletion(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.markDeletionRequested();
        userRepository.save(user);
        cacheService.evictByPattern("users:*");
        auditPublisher.publish("USER_DELETION_REQUESTED", userId, "User",
            null, Map.of("status", UserStatus.PENDING_DELETION.name()));
    }

    @Override
    @Transactional
    public void assignRole(Long userId, Role role, Long actorID) {
        var user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isDisabled()) {
            throw new InvalidRoleAssignmentException("Cannot assign roles to a disabled user");
        }
        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new InvalidRoleAssignmentException("User already has role: " + role);
        }

        userRoleRepository.save(new UserRole(userId, role));
        cacheService.evictByPattern(USER_ROLES);
        auditPublisher.publish(userId, "ROLE_ASSIGNED", userId, "User", null, Map.of("role", role.name(), "userId", userId));
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
        cacheService.evictByPattern(USER_ROLES);
        auditPublisher.publish("ROLE_REVOKED", userId, "User", Map.of("role", role.name()), null);
    }

    @Override
    @Transactional
    public void verifyEmail(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new ValidationException("Email already verified", UserErrorCode.USER_NOT_FOUND.name());
        }
        user.verifyEmail();
        user.updateStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        cacheService.evictByPattern("users:*");
        auditPublisher.publish(userId, "USER_EMAIL_VERIFIED", userId, "User",
            Map.of("status", user.getStatus().name()),
            Map.of("emailVerified", true));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (user.isDeleted()) {
            throw new CannotDeleteUserException(id);
        }
        user.markDeleted();
        userRepository.save(user);
        userRoleRepository.deleteByUserId(id);
        cacheService.evictByPattern("users:*");
        cacheService.evictByPattern("userExists:*");
        cacheService.evictByPattern(USER_ROLES);
        auditPublisher.publish("USER_DELETED", id, "User", Map.of("email", user.getEmail()), null);
    }

    @Override
    @Transactional
    public void seedSuperAdminIfAbsent() {
        if (superAdminEmail == null || superAdminEmail.isBlank()
                || superAdminPhone == null || superAdminPhone.isBlank()
                || superAdminPassword == null || superAdminPassword.isBlank()) return;
        if (userRepository.existsByEmail(superAdminEmail)) return;
        var user = create(superAdminEmail, superAdminPhone, superAdminPassword, "Super Admin", "Mourad Selim", "Jnayeh");
        userRoleRepository.save(new UserRole(user.getId(), Role.SUPER_ADMIN));
        userRoleRepository.save(new UserRole(user.getId(), Role.CENTER_ADMIN));
        userRoleRepository.save(new UserRole(user.getId(), Role.CENTER_STAFF));
        userRoleRepository.save(new UserRole(user.getId(), Role.DONOR));
    }

    @Override
    public User findById(Long id) {
        var key = "users:" + id;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached.get();
        var result = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        cacheService.put(key, result);
        return result;
    }

    @Override
    public User findByEmail(String email) {
        var key = "users:email:" + email;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached.get();
        var result = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        cacheService.put(key, result);
        return result;
    }

    @Override
    public User findByPhone(String phone) {
        var key = "users:phone:" + phone;
        var cached = cacheService.get(key, User.class);
        if (cached.isPresent()) return cached.get();
        var result = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UserNotFoundException(phone));
        cacheService.put(key, result);
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
