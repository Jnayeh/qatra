package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.user.domain.model.Session;
import com.zayenha.qatra.user.domain.port.out.SessionRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.entity.SessionEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.SessionJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionRepositoryAdapter implements SessionRepositoryPort {

    private final SessionJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public Session save(Session session) {
        var entity = toEntity(session);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Session> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Session> findByAccessTokenHash(String accessTokenHash) {
        return jpaRepository.findByAccessTokenHash(accessTokenHash).map(this::toDomain);
    }

    @Override
    public Optional<Session> findByRefreshTokenHash(String refreshTokenHash) {
        return jpaRepository.findByRefreshTokenHash(refreshTokenHash).map(this::toDomain);
    }

    @Override
    public Optional<Session> findActiveByUserId(Long userId) {
        return jpaRepository.findAll().stream()
                .filter(e -> e.getUser().getId().equals(userId))
                .filter(e -> e.getExpiresAt().isAfter(java.time.Instant.now()))
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Session toDomain(SessionEntity e) {
        return Session.reconstruct(
            e.getId(), e.getUser().getId(),
            e.getAccessTokenHash(), e.getRefreshTokenHash(),
            e.getIpAddress(), e.getUserAgent(),
            e.getExpiresAt(), e.getCreatedAt()
        );
    }

    private SessionEntity toEntity(Session s) {
        var e = new SessionEntity();
        if (s.getId() != null) e.setId(s.getId());
        e.setUser(userJpaRepository.getReferenceById(s.getUserId()));
        e.setAccessTokenHash(s.getAccessTokenHash());
        e.setRefreshTokenHash(s.getRefreshTokenHash());
        e.setIpAddress(s.getIpAddress());
        e.setUserAgent(s.getUserAgent());
        e.setExpiresAt(s.getExpiresAt());
        e.setCreatedAt(s.getCreatedAt());
        return e;
    }
}
