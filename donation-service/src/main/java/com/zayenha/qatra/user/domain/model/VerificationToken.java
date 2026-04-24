package com.zayenha.qatra.user.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class VerificationToken {
    private Long id;
    private Long userId;
    private String tokenHash;
    private VerificationTokenType type;
    private Instant expiresAt;
    private Instant createdAt;

    public VerificationToken() {}

    public VerificationToken(Long userId, String tokenHash, VerificationTokenType type,
                             Instant expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.type = type;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public static VerificationToken reconstruct(Long id, Long userId, String tokenHash,
                                                VerificationTokenType type, Instant expiresAt,
                                                Instant createdAt) {
        var t = new VerificationToken();
        t.id = id;
        t.userId = userId;
        t.tokenHash = tokenHash;
        t.type = type;
        t.expiresAt = expiresAt;
        t.createdAt = createdAt;
        return t;
    }

    public boolean validate() {
        return Instant.now().isBefore(expiresAt);
    }

    public void consume() {
        this.expiresAt = Instant.now();
    }
}
