package com.zayenha.qatra.user.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.user.api.dto.UserCreatedEvent;
import com.zayenha.qatra.user.domain.exception.CannotDeleteActiveUserException;
import com.zayenha.qatra.user.domain.exception.EmailAlreadyExistsException;
import com.zayenha.qatra.user.domain.exception.InvalidRoleAssignmentException;
import com.zayenha.qatra.user.domain.exception.UserNotFoundException;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserRole;
import com.zayenha.qatra.user.domain.model.UserStatus;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.zayenha.qatra.user.domain.port.out.UserRoleRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private UserRoleRepositoryPort userRoleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CacheService cacheService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userRoleRepository, passwordEncoder, eventPublisher, cacheService);
    }

    private User aUser() {
        return User.reconstruct(1L, "test@example.com", "1234567890",
                "encoded", "Test User", UserStatus.ACTIVE, false,
                null, java.time.Instant.now(), null, List.of());
    }

    private User anInactiveUser() {
        return User.reconstruct(2L, "inactive@example.com", "0987654321",
                "encoded", "Inactive User", UserStatus.INACTIVE, false,
                null, java.time.Instant.now(), null, List.of());
    }

    // --- create ---

    @Test
    void createSavesUserAndPublishesEvent() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("1234567890")).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> {
            var user = invocation.<User>getArgument(0);
            return User.reconstruct(1L, user.getEmail(), user.getPhone(),
                    user.getHashedPassword(), user.getDisplayName(),
                    user.getStatus(), user.isEmailVerified(),
                    null, user.getCreatedAt(), null, user.getRoles());
        });

        var result = userService.create("new@example.com", "1234567890", "rawPassword", "New User");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getHashedPassword()).isEqualTo("encodedPassword");
        verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
    }

    @Test
    void createThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.create("dup@example.com", "1234567890", "pass", "Dup"))
                .isInstanceOf(EmailAlreadyExistsException.class);
        verify(userRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // --- update ---

    @Test
    void updateUpdatesExistingUser() {
        var existing = aUser();
        when(userRepository.existsOtherByEmailOrPhone(1L, "new@example.com", "0987654321")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = userService.update(1L, "new@example.com", "0987654321", "Updated Name");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getPhone()).isEqualTo("0987654321");
        assertThat(result.getDisplayName()).isEqualTo("Updated Name");
    }

    @Test
    void updateThrowsWhenUserNotFound() {
        when(userRepository.existsOtherByEmailOrPhone(99L, "x@x.com", "000")).thenReturn(false);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, "x@x.com", "000", "X"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- updateStatus ---

    @Test
    void updateStatusChangesStatus() {
        var user = aUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.updateStatus(1L, UserStatus.SUSPENDED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.SUSPENDED);
    }

    @Test
    void updateStatusThrowsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateStatus(99L, UserStatus.INACTIVE))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- assignRole ---

    @Test
    void assignRoleSavesRoleWhenValid() {
        var user = aUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleRepository.existsByUserIdAndRole(1L, Role.DONOR)).thenReturn(false);

        userService.assignRole(1L, Role.DONOR);

        verify(userRoleRepository).save(any(UserRole.class));
    }

    @Test
    void assignRoleThrowsWhenUserInactive() {
        var user = anInactiveUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.assignRole(2L, Role.DONOR))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("inactive");
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void assignRoleThrowsWhenRoleAlreadyAssigned() {
        var user = aUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRoleRepository.existsByUserIdAndRole(1L, Role.DONOR)).thenReturn(true);

        assertThatThrownBy(() -> userService.assignRole(1L, Role.DONOR))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("already has role");
        verify(userRoleRepository, never()).save(any());
    }

    // --- revokeRole ---

    @Test
    void revokeRoleDeletesRoleWhenValid() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRoleRepository.existsByUserIdAndRole(1L, Role.DONOR)).thenReturn(true);

        userService.revokeRole(1L, Role.DONOR);

        verify(userRoleRepository).deleteByUserIdAndRole(1L, Role.DONOR);
    }

    @Test
    void revokeRoleThrowsWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.revokeRole(99L, Role.DONOR))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void revokeRoleThrowsWhenRoleNotAssigned() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRoleRepository.existsByUserIdAndRole(1L, Role.DONOR)).thenReturn(false);

        assertThatThrownBy(() -> userService.revokeRole(1L, Role.DONOR))
                .isInstanceOf(InvalidRoleAssignmentException.class)
                .hasMessageContaining("does not have role");
    }

    // --- delete ---

    @Test
    void deleteMarksInactiveUserAsDeleted() {
        var user = anInactiveUser();
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.delete(2L);

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();
        verify(userRoleRepository).deleteByUserId(2L);
    }

    @Test
    void deleteThrowsWhenUserIsActive() {
        var user = aUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(CannotDeleteActiveUserException.class);
        verify(userRoleRepository, never()).deleteByUserId(anyLong());
    }

    @Test
    void deleteThrowsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // --- findById ---

    @Test
    void findByIdReturnsUser() {
        var user = aUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        var result = userService.findById(1L);

        assertThat(result).isPresent().contains(user);
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(userService.findById(99L)).isEmpty();
    }

    // --- findByEmail / findByPhone ---

    @Test
    void findByEmailDelegatesToRepository() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(aUser()));

        assertThat(userService.findByEmail("test@example.com")).isPresent();
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void findByPhoneDelegatesToRepository() {
        when(userRepository.findByPhone("1234567890")).thenReturn(Optional.of(aUser()));

        assertThat(userService.findByPhone("1234567890")).isPresent();
        verify(userRepository).findByPhone("1234567890");
    }

    // --- existsByEmail / existsByPhone ---

    @Test
    void existsByEmailDelegatesToRepository() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThat(userService.existsByEmail("test@example.com")).isTrue();
    }

    @Test
    void existsByPhoneDelegatesToRepository() {
        when(userRepository.existsByPhone("1234567890")).thenReturn(true);

        assertThat(userService.existsByPhone("1234567890")).isTrue();
    }

    // --- getUserRoles ---

    @Test
    void getUserRolesReturnsMappedRoles() {
        var userRoles = List.of(
                UserRole.reconstruct(1L, 1L, Role.DONOR, java.time.Instant.now()),
                UserRole.reconstruct(2L, 1L, Role.CENTER_STAFF, java.time.Instant.now())
        );
        when(userRoleRepository.findByUserId(1L)).thenReturn(userRoles);

        var result = userService.getUserRoles(1L);

        assertThat(result).containsExactly(Role.DONOR, Role.CENTER_STAFF);
    }

    // --- findAll ---

    @Test
    void findAllDelegatesToRepository() {
        var criteria = new SearchCriteria(null, "id", "asc", 0, 20);
        var pageResult = new PageResult<User>(List.of(aUser()), 0, 20, 1, 1);
        when(userRepository.findAll(criteria)).thenReturn(pageResult);

        var result = userService.findAll(criteria);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    // --- seedSuperAdminIfAbsent ---

    @Test
    void seedSuperAdminSkipsWhenEnvVarsMissing() {
        userService.seedSuperAdminIfAbsent();

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }
}
