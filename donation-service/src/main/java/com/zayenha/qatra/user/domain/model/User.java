package com.zayenha.qatra.user.domain.model;

import com.zayenha.qatra._shared.UserStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
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
    private Instant deletionRequestedAt;
    private Instant deletedAt;
    private List<Role> roles;

    public User(String email, String phone, String hashedPassword, String displayName,
                String firstName, String familyName) {
        this.email = email;
        this.phone = phone;
        this.hashedPassword = hashedPassword;
        this.displayName = (displayName != null && !displayName.isBlank()) ? displayName : firstName + " " + familyName;
        this.firstName = firstName;
        this.familyName = familyName;
        this.status = UserStatus.PENDING_VERIFICATION;
        this.emailVerified = false;
        this.createdAt = Instant.now();
        this.roles = List.of();
    }

    public User() {}

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

    public void markDeletionRequested() {
        this.status = UserStatus.PENDING_DELETION;
        this.deletionRequestedAt = Instant.now();
    }

    public void changePassword(String newEncodedPassword) {
        this.hashedPassword = newEncodedPassword;
    }

    public void verifyEmail() { this.emailVerified = true; }

    public boolean isActive() { return status == UserStatus.ACTIVE; }
    public boolean isEnabled() { return !isDisabled(); }
    public boolean isDisabled() { return status == UserStatus.INACTIVE
        || status == UserStatus.SUSPENDED
        || status == UserStatus.DELETED; }
    public boolean isDeleted() { return status == UserStatus.DELETED; }
}
