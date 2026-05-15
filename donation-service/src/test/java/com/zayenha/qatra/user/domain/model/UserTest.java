package com.zayenha.qatra.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private static final Long ID = 1L;
    private static final String EMAIL = "test@example.com";
    private static final String PHONE = "1234567890";
    private static final String PASSWORD = "encoded-password";
    private static final String DISPLAY_NAME = "Test User";

    @Test
    void constructorCreatesPendingVerificationUser() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");

        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getHashedPassword()).isEqualTo(PASSWORD);
        assertThat(user.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getRoles()).isEmpty();
        assertThat(user.isActive()).isFalse();
        assertThat(user.isDeleted()).isFalse();
    }

    @Test
    void reconstructRestoresAllFields() {
        var now = Instant.now();
        var roles = List.of(Role.DONOR);

        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "John", "Doe");
        user.setId(ID);
        user.setStatus(UserStatus.INACTIVE);
        user.setEmailVerified(true);
        user.setCreatedAt(now);
        user.setLastActiveAt(now);
        user.setDeletedAt(now);
        user.setRoles(roles);

        assertThat(user.getId()).isEqualTo(ID);
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getPhone()).isEqualTo(PHONE);
        assertThat(user.getHashedPassword()).isEqualTo(PASSWORD);
        assertThat(user.getDisplayName()).isEqualTo(DISPLAY_NAME);
        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.getDeletedAt()).isEqualTo(now);
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getLastActiveAt()).isEqualTo(now);
        assertThat(user.getRoles()).containsExactly(Role.DONOR);
    }

    @Test
    void updateChangesScalarFields() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        user.update("new@example.com", "0987654321", "New Name");

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPhone()).isEqualTo("0987654321");
        assertThat(user.getDisplayName()).isEqualTo("New Name");
    }

    @Test
    void updateStatusChangesStatus() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        user.updateStatus(UserStatus.INACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void updateStatusThrowsWhenUserIsDeleted() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        user.markDeleted();

        assertThatThrownBy(() -> user.updateStatus(UserStatus.ACTIVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("deleted");
    }

    @Test
    void markDeletedSetsStatusAndDeletedAt() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        user.markDeleted();

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
        assertThat(user.getDeletedAt()).isNotNull();
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    void verifyEmailSetsFlag() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        user.verifyEmail();

        assertThat(user.isEmailVerified()).isTrue();
    }

    @Test
    void isActiveReturnsTrueOnlyForActiveStatus() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        assertThat(user.isActive()).isFalse();

        user.updateStatus(UserStatus.ACTIVE);
        assertThat(user.isActive()).isTrue();

        user.updateStatus(UserStatus.INACTIVE);
        assertThat(user.isActive()).isFalse();

        user.updateStatus(UserStatus.SUSPENDED);
        assertThat(user.isActive()).isFalse();
    }

    @Test
    void isDeletedReturnsTrueOnlyForDeletedStatus() {
        var user = new User(EMAIL, PHONE, PASSWORD, DISPLAY_NAME, "", "");
        assertThat(user.isDeleted()).isFalse();

        user.markDeleted();
        assertThat(user.isDeleted()).isTrue();
    }
}
