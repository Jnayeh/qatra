package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.domain.model.FeatureFlag;
import com.zayenha.qatra.system.domain.port.out.FeatureFlagRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureFlagService {

    private final FeatureFlagRepositoryPort repository;

    @Transactional
    public FeatureFlag create(String flagName, boolean enabled, String description) {
        return repository.save(new FeatureFlag(flagName, enabled, description));
    }

    @Transactional
    public FeatureFlag enable(String flagName) {
        var flag = findOrThrow(flagName);
        flag.enable();
        return repository.save(flag);
    }

    @Transactional
    public FeatureFlag disable(String flagName) {
        var flag = findOrThrow(flagName);
        flag.disable();
        return repository.save(flag);
    }

    @Transactional(readOnly = true)
    public boolean isEnabled(String flagName) {
        return repository.isEnabled(flagName);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> getAll() {
        return repository.findAll();
    }

    private FeatureFlag findOrThrow(String flagName) {
        return repository.findByFlagName(flagName)
                .orElseThrow(() -> new NotFoundException("Feature flag not found: " + flagName, "FEATURE_FLAG_NOT_FOUND"));
    }
}
