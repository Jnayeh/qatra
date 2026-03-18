package com.zayenha.qatra.system.infrastructure.persistence.repository;

import com.zayenha.qatra.system.infrastructure.persistence.entity.FeatureFlagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeatureFlagJpaRepository extends JpaRepository<FeatureFlagEntity, Long> {
    Optional<FeatureFlagEntity> findByFlagName(String flagName);
}
