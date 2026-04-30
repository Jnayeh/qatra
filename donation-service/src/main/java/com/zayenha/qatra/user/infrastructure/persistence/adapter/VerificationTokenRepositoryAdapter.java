package com.zayenha.qatra.user.infrastructure.persistence.adapter;

import com.zayenha.qatra.user.domain.model.VerificationToken;
import com.zayenha.qatra.user.domain.model.VerificationTokenType;
import com.zayenha.qatra.user.domain.port.out.VerificationTokenRepositoryPort;
import com.zayenha.qatra.user.infrastructure.persistence.entity.VerificationTokenEntity;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.repository.VerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VerificationTokenRepositoryAdapter implements VerificationTokenRepositoryPort {

    private final VerificationTokenJpaRepository jpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public VerificationToken save(VerificationToken token) {
        var entity = toEntity(token);
        var saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<VerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public Optional<VerificationToken> findByUserIdAndType(Long userId, VerificationTokenType type) {
        return jpaRepository.findByUserIdAndType(userId, type).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private VerificationToken toDomain(VerificationTokenEntity e) {
        return VerificationToken.reconstruct(
            e.getId(), e.getUser().getId(),
            e.getTokenHash(), e.getType(),
            e.getExpiresAt(), e.getCreatedAt()
        );
    }

    private VerificationTokenEntity toEntity(VerificationToken t) {
        var e = new VerificationTokenEntity();
        if (t.getId() != null) e.setId(t.getId());
        e.setUser(userJpaRepository.getReferenceById(t.getUserId()));
        e.setTokenHash(t.getTokenHash());
        e.setType(t.getType());
        e.setExpiresAt(t.getExpiresAt());
        e.setCreatedAt(t.getCreatedAt());
        return e;
    }
}
