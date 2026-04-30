package com.zayenha.qatra.user.domain.model;

import lombok.Getter;

import java.time.Instant;

@Getter
public class Session {
    private Long id;
    private Long userId;
    private String accessTokenHash;
    private String refreshTokenHash;
    private String ipAddress;
    private String userAgent;
    private Instant expiresAt;
    private Instant createdAt;

    public Session() {}

    public Session(Long userId, String accessTokenHash, String refreshTokenHash,
                   String ipAddress, String userAgent, Instant expiresAt) {
        this.userId = userId;
        this.accessTokenHash = accessTokenHash;
        this.refreshTokenHash = refreshTokenHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
        this.createdAt = Instant.now();
    }

    public static Session reconstruct(Long id, Long userId, String accessTokenHash,
                                      String refreshTokenHash, String ipAddress,
                                      String userAgent, Instant expiresAt,
                                      Instant createdAt) {
        var s = new Session();
        s.id = id;
        s.userId = userId;
        s.accessTokenHash = accessTokenHash;
        s.refreshTokenHash = refreshTokenHash;
        s.ipAddress = ipAddress;
        s.userAgent = userAgent;
        s.expiresAt = expiresAt;
        s.createdAt = createdAt;
        return s;
    }

    public boolean validate() {
        return Instant.now().isBefore(expiresAt);
    }

    public void refresh(Instant newExpiresAt) {
        this.expiresAt = newExpiresAt;
    }

    public void revoke() {
        this.expiresAt = Instant.now();
    }

    public void rotateTokens(String newAccessTokenHash, String newRefreshTokenHash, Instant newExpiresAt) {
        this.accessTokenHash = newAccessTokenHash;
        this.refreshTokenHash = newRefreshTokenHash;
        this.expiresAt = newExpiresAt;
    }
}
