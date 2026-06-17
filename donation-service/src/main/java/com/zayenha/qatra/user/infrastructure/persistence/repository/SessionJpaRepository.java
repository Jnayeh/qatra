package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra.user.infrastructure.persistence.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SessionJpaRepository extends JpaRepository<SessionEntity, Long> {

    Optional<SessionEntity> findByAccessTokenHash(String accessTokenHash);

    Optional<SessionEntity> findByRefreshTokenHash(String refreshTokenHash);

    void deleteByExpiresAtBefore(Instant cutoff);
}
