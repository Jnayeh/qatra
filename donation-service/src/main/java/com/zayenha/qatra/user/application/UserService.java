package com.zayenha.qatra.user.application;

import com.zayenha.qatra.shared.domain.PageResult;
import com.zayenha.qatra.shared.domain.SearchCriteria;
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
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import com.zayenha.qatra.user.domain.service.UserDomainValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    private UserDomainValidator validator() {
        return new UserDomainValidator(userRepository);
    }

    @Override
    @Transactional
    public User create(String email, String phone, String password, String displayName) {
        validator().validateCreate(email, phone);
        var user = new User(email, phone, passwordEncoder.encode(password), displayName);
        user = userRepository.save(user);
        eventPublisher.publishEvent(
                new UserCreatedEvent(this, user.getId(), user.getEmail())
        );
        log.info("AUDIT [actor={}] action={} details={}", user.getId(), "USER_CREATED", "email=" + email);
        return user;
    }

    @Override
    @Transactional
    public User update(Long id, String email, String phone, String displayName) {
        validator().validateUpdate(id, email, phone);
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        log.info("validated");
        user.update(email, phone, displayName);
        user = userRepository.save(user); //UP
        log.info("AUDIT [actor={}] action={} details={}", id, "USER_UPDATED", "email=" + email);
        return user;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, UserStatus status) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.updateStatus(status);
        userRepository.save(user);
        log.info("AUDIT [actor={}] action={} details={}", id, "USER_STATUS_CHANGED", "newStatus=" + status);
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
        log.info("AUDIT [actor={}] action={} details={}", userId, "ROLE_ASSIGNED", "role=" + role);
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
        log.info("AUDIT [actor={}] action={} details={}", userId, "ROLE_REVOKED", "role=" + role);
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
        log.info("AUDIT [actor={}] action={} details={}", id, "USER_DELETED", "");
    }

    @Override
    @Transactional
    public void seedSuperAdminIfAbsent() {
        var email = System.getenv("SUPER_ADMIN_EMAIL");
        var phone = System.getenv("SUPER_ADMIN_PHONE");
        var password = System.getenv("SUPER_ADMIN_PASSWORD");
        if (email == null || phone == null || password == null) return;
        if (userRepository.existsByEmail(email)) {
            return;
        }
        var user = create(email, phone, password, "Super Admin");
        userRoleRepository.save(new UserRole(user.getId(), Role.SUPER_ADMIN));
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
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
