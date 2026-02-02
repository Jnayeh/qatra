package com.zayenha.qatra.user.infrastructure.web.mapper;

import com.zayenha.qatra.user.domain.model.Role;
import com.zayenha.qatra.user.domain.model.User;
import com.zayenha.qatra.user.domain.model.UserStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private User aUser() {
        return User.reconstruct(1L, "test@example.com", "1234567890",
                "encoded", "Test User", UserStatus.ACTIVE, true,
                null, Instant.now(), Instant.now(), List.of(Role.DONOR));
    }

    @Test
    void toDetailMapsAllFields() {
        var user = aUser();
        var detail = UserMapper.toDetail(user, user.getRoles());

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
        var user = User.reconstruct(1L, "a@b.com", "000", "enc", "No Roles",
                UserStatus.ACTIVE, false, null, Instant.now(), null, List.of());
        var detail = UserMapper.toDetail(user, List.of());

        assertThat(detail.roles()).isEmpty();
        assertThat(detail.lastActiveAt()).isNull();
    }

}
