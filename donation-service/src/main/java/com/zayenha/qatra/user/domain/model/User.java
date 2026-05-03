package com.zayenha.qatra.user.domain.model;

import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class User {
    private Long id;
    private String email;
    private String phone;
    private String hashedPassword;
    private String displayName;
    private String firstName;
    private String familyName;
    private UserStatus status;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant lastActiveAt;
    private Instant deletedAt;
    private List<Role> roles;

    public User(String email, String phone, String hashedPassword, String displayName,
                String firstName, String familyName) {
        this.email = email;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.displayName = displayName;
        this.firstName = firstName;
        this.familyName = familyName;
        this.status = UserStatus.ACTIVE;
        this.emailVerified = false;
        this.createdAt = Instant.now();
        this.roles = List.of();
    }

    public User(String email, String phone, String hashedPassword, String displayName) {
        this(email, phone, hashedPassword, displayName, null, null);
    }

    private User() {}

    public static User reconstruct(Long id, String email, String phone,
                                   String hashedPassword, String displayName,
                                   String firstName, String familyName,
                                   UserStatus status, boolean emailVerified,
                                   Instant createdAt, Instant lastActiveAt,
                                   Instant deletedAt, List<Role> roles) {
        var u = new User();
        u.id = id;
        u.email = email;
        u.phone = phone;
        u.hashedPassword = hashedPassword;
        u.displayName = displayName;
        u.firstName = firstName;
        u.familyName = familyName;
        u.status = status;
        u.emailVerified = emailVerified;
        u.createdAt = createdAt;
        u.lastActiveAt = lastActiveAt;
        u.deletedAt = deletedAt;
        u.roles = roles != null ? roles : List.of();
        return u;
    }

    public void update(String email, String phone, String displayName,
                       String firstName, String familyName) {
        this.email = email;
        this.phone = phone;
        this.displayName = displayName;
        this.firstName = firstName;
        this.familyName = familyName;
    }

    public void update(String email, String phone, String displayName) {
        update(email, phone, displayName, null, null);
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

    public void changePassword(String newEncodedPassword) {
        this.hashedPassword = newEncodedPassword;
    }

    public void verifyEmail() { this.emailVerified = true; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }
    public boolean isDeleted() { return status == UserStatus.DELETED; }
}
