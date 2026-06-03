package com.zayenha.qatra.user.infrastructure.web;

import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.GlobalExceptionHandler;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.user.domain.exception.*;
import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserStatus;
import com.zayenha.qatra.user.domain.port.in.UserCommandUseCases;
import com.zayenha.qatra.user.domain.port.in.UserQueryUseCases;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapper;
import com.zayenha.qatra.user.infrastructure.web.dto.request.*;
import com.zayenha.qatra.user.infrastructure.web.dto.response.UserDetailResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserCommandUseCases commandUseCases;
    @Mock
    private UserQueryUseCases queryUseCases;
    @Mock
    private UserMapper mapper;

    private UserController controller;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        controller = new UserController(commandUseCases, queryUseCases, mapper);
        exceptionHandler = new GlobalExceptionHandler();
    }

    private User aUser() {
        var user = new User("test@example.com", "1234567890", "encoded", "Test User", "John", "Doe");
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setRoles(List.of(Role.DONOR));
        return user;
    }

    @Test
    void listAllReturnsPaginatedUsers() {
        var user = aUser();
        var result = new com.zayenha.qatra._shared.domain.PageResult<User>(
                List.of(user), 0, 20, 1, 1);
        when(queryUseCases.findAll(any(SearchCriteria.class))).thenReturn(result);

        var response = controller.listAll(null, "id", "asc", 1, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).hasSize(1);
        assertThat(response.getBody().page()).isNotNull();
        assertThat(response.getBody().page().number()).isEqualTo(1);
    }

    @Test
    void getDetailsReturnsUser() {
        var user = aUser();
        when(queryUseCases.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toDetail(user)).thenReturn(
            new UserDetailResponse(user.getId(), user.getEmail(), user.getPhone(),
                user.getDisplayName(), user.getStatus(), user.isEmailVerified(),
                user.getRoles(), user.getCreatedAt(), user.getDeletionRequestedAt(),
                user.getDeletedAt(), user.getLastActiveAt()));

        var response = controller.getDetails(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(1L);
        assertThat(response.getBody().data().email()).isEqualTo("test@example.com");
        assertThat(response.getBody().data().roles()).containsExactly(Role.DONOR);
    }

    @Test
    void getDetailsThrowsWhenNotFound() {
        when(queryUseCases.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getDetails(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createReturnsCreatedUser() {
        var user = aUser();
        var request = new CreateUserRequest("test@example.com", "1234567890", "password123", "Test User", "John", "Doe");
        when(commandUseCases.create(request.email(), request.phone(), request.password(), request.displayName(), request.firstName(), request.familyName()))
                .thenReturn(user);
        when(mapper.toDetail(user)).thenReturn(
            new UserDetailResponse(user.getId(), user.getEmail(), user.getPhone(),
                user.getDisplayName(), user.getStatus(), user.isEmailVerified(),
                user.getRoles(), user.getCreatedAt(), user.getDeletionRequestedAt(),
                user.getDeletedAt(), user.getLastActiveAt()));

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data().id()).isEqualTo(1L);
    }

    @Test
    void updateReturnsUpdatedUser() {
        var user = aUser();
        when(commandUseCases.update(eq(1L), anyString(), anyString(), anyString())).thenReturn(user);

        var request = new UpdateUserRequest("test@example.com", "1234567890", "Updated");
        var response = controller.update(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
    }

    @Test
    void updateStatusReturnsOk() {
        var request = new UpdateUserStatusRequest(UserStatus.INACTIVE);
        var response = controller.updateStatus(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commandUseCases).updateStatus(1L, UserStatus.INACTIVE, AuditUtils.currentUserId());
    }

    @Test
    void assignRoleReturnsOk() {
        var request = new AssignRoleRequest(Role.DONOR);
        var response = controller.assignRole(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(commandUseCases).assignRole(1L, Role.DONOR, AuditUtils.currentUserId());
    }

    @Test
    void revokeRoleReturnsOk() {
        var request = new RevokeRoleRequest(Role.DONOR);
        var response = controller.revokeRole(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo("Role revoked");
    }

    @Test
    void deleteReturnsOk() {
        var response = controller.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isEqualTo("User deleted");
        verify(commandUseCases).delete(1L);
    }

    // --- ExceptionHandler tests ---

    @Test
    void userNotFoundReturns404() {
        var ex = new UserNotFoundException(99L);
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("99");
    }

    @Test
    void alreadyExistsReturns409() {
        var ex = new EmailAlreadyExistsException("dup@example.com");
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).isEqualTo("Email already in use: dup@example.com");
    }

    @Test
    void cannotDeleteActiveUserReturns422() {
        var ex = new CannotDeleteUserException(1L);
        var response = exceptionHandler.handleBase(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().message()).contains("Cannot delete user");
    }
}
