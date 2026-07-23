package com.zayenha.qatra.user.infrastructure.web.mapper;

import com.zayenha.qatra._shared.domain.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra._shared.domain.UserStatus;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapper;
import com.zayenha.qatra.user.infrastructure.mapper.UserMapperImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl();
    }

    private User aUser() {
        var user = new User("test@example.com", "1234567890", "encoded", "Test User", "John", "Doe");
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);
        user.setCreatedAt(Instant.now());
        user.setLastActiveAt(Instant.now());
        user.setRoles(List.of(Role.DONOR));
        return user;
    }

    @Test
    void toDetailMapsAllFields() {
        var user = aUser();
        var detail = mapper.toDetail(user);

        assertThat(detail.id()).isEqualTo(1L);
        assertThat(detail.email()).isEqualTo("test@example.com");
        assertThat(detail.phone()).isEqualTo("1234567890");
        assertThat(detail.displayName()).isEqualTo("Test User");
        assertThat(detail.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(detail.emailVerified()).isTrue();
        assertThat(detail.roles()).containsExactly(Role.DONOR);
        assertThat(detail.createdAt()).isNotNull();
        assertThat(detail.lastActiveAt()).isNotNull();
    }

    @Test
    void toDetailAcceptsEmptyRoles() {
        var user = new User("a@b.com", "000", "enc", "No Roles", "First", "Last");
        user.setId(1L);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(false);
        user.setCreatedAt(Instant.now());
        user.setRoles(List.of());
        var detail = mapper.toDetail(user);

        assertThat(detail.roles()).isEmpty();
        assertThat(detail.lastActiveAt()).isNull();
    }

}
