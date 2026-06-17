package com.zayenha.qatra.user.domain.port.out;

import com.zayenha.qatra.user.domain.model.Session;

import java.util.Optional;

public interface SessionRepositoryPort {

    Session save(Session session);

    Optional<Session> findById(Long id);

    Optional<Session> findByAccessTokenHash(String accessTokenHash);

    Optional<Session> findByRefreshTokenHash(String refreshTokenHash);

    Optional<Session> findActiveByUserId(Long userId);

    void deleteById(Long id);

    void deleteExpiredSessions(java.time.Instant cutoff);
}
