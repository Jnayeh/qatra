package com.zayenha.qatra.user.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class UserRoleTest {

    @Test
    void constructorCreatesUserRole() {
        var userRole = new UserRole(1L, Role.DONOR);

        assertThat(userRole.getId()).isNull();
        assertThat(userRole.getUserId()).isEqualTo(1L);
        assertThat(userRole.getRole()).isEqualTo(Role.DONOR);
        assertThat(userRole.getAssignedAt()).isNotNull();
    }

    @Test
    void reconstructRestoresAllFields() {
        var now = Instant.now();
        var userRole = UserRole.reconstruct(1L, 2L, Role.CENTER_ADMIN, now);

        assertThat(userRole.getId()).isEqualTo(1L);
        assertThat(userRole.getUserId()).isEqualTo(2L);
        assertThat(userRole.getRole()).isEqualTo(Role.CENTER_ADMIN);
        assertThat(userRole.getAssignedAt()).isEqualTo(now);
    }
}
