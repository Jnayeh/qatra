package com.zayenha.qatra.user.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class User {
    private Long id;
    private String email;
    private String phone;
    private String hashedPassword;
    private String displayName;
    private UserStatus status;
    private boolean emailVerified;
    private Instant deletedAt;
    private Instant createdAt;
    private Instant lastActiveAt;

    public User(String email, String phone, String hashedPassword, String displayName) {
        this.email = email;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.displayName = displayName;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.createdAt = Instant.now();
    }

    private User() {}

    public static User reconstruct(Long id, String email, String phone,
                                   String hashedPassword, String displayName,
                                   UserStatus status, boolean emailVerified,
                                   Instant deletedAt, Instant createdAt,
                                   Instant lastActiveAt) {
        var u = new User();
        u.id = id;
        u.email = email;
        u.phone = phone;
        u.hashedPassword = hashedPassword;
        u.displayName = displayName;
        u.status = status;
        u.emailVerified = emailVerified;
        u.deletedAt = deletedAt;
        u.createdAt = createdAt;
        u.lastActiveAt = lastActiveAt;
        return u;
    }

    public void update(String email, String phone, String displayName) {
        this.email = email;
        this.phone = phone;
        this.displayName = displayName;
    }

    public void updateStatus(UserStatus status) {
        if (this.status == UserStatus.DELETED) {
            throw new IllegalStateException("Cannot change status of a deleted user");
        }
        this.status = status;
    }

    public void markDeleted() {
        this.status = UserStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    public void verifyEmail() { this.emailVerified = true; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }
    public boolean isDeleted() { return status == UserStatus.DELETED; }

}
