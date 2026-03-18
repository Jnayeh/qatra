package com.zayenha.qatra.system.domain.port.out;

import com.zayenha.qatra.system.domain.model.FeatureFlag;

import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepositoryPort {
    FeatureFlag save(FeatureFlag flag);
    Optional<FeatureFlag> findByFlagName(String flagName);
    List<FeatureFlag> findAll();
    boolean isEnabled(String flagName);
}
