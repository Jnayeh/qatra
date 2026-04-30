package com.zayenha.qatra.user.domain.port.out;

import com.zayenha.qatra.user.domain.model.VerificationToken;
import com.zayenha.qatra.user.domain.model.VerificationTokenType;

import java.util.Optional;

public interface VerificationTokenRepositoryPort {

    VerificationToken save(VerificationToken token);

    Optional<VerificationToken> findByTokenHash(String tokenHash);

    Optional<VerificationToken> findByUserIdAndType(Long userId, VerificationTokenType type);

    void deleteById(Long id);
}
