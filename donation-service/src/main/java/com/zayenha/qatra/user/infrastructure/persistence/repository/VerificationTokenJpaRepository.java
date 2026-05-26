package com.zayenha.qatra.user.infrastructure.persistence.repository;

import com.zayenha.qatra.user.domain.model.verification.VerificationTokenType;
import com.zayenha.qatra.user.infrastructure.persistence.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenJpaRepository extends JpaRepository<VerificationTokenEntity, Long> {

    Optional<VerificationTokenEntity> findByTokenHash(String tokenHash);

    Optional<VerificationTokenEntity> findByUserIdAndType(Long userId, VerificationTokenType type);
}
