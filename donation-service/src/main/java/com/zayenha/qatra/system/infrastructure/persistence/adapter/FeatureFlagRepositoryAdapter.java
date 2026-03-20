package com.zayenha.qatra.system.infrastructure.persistence.adapter;

import com.zayenha.qatra.system.domain.model.FeatureFlag;
import com.zayenha.qatra.system.domain.port.out.FeatureFlagRepositoryPort;
import com.zayenha.qatra.system.infrastructure.persistence.entity.FeatureFlagEntity;
import com.zayenha.qatra.system.infrastructure.persistence.repository.FeatureFlagJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FeatureFlagRepositoryAdapter implements FeatureFlagRepositoryPort {

    private final FeatureFlagJpaRepository jpaRepository;

    @Override
    public FeatureFlag save(FeatureFlag flag) {
        var entity = toEntity(flag);
        if (entity.getId() != null) {
            var existing = jpaRepository.findById(entity.getId()).orElseThrow();
            existing.setEnabled(entity.isEnabled());
            existing.setDescription(entity.getDescription());
            return toDomain(jpaRepository.save(existing));
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<FeatureFlag> findByFlagName(String flagName) {
        return jpaRepository.findByFlagName(flagName).map(this::toDomain);
    }

    @Override
    public List<FeatureFlag> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public boolean isEnabled(String flagName) {
        return jpaRepository.findByFlagName(flagName).map(FeatureFlagEntity::isEnabled).orElse(false);
    }

    private FeatureFlagEntity toEntity(FeatureFlag domain) {
        var entity = new FeatureFlagEntity();
        entity.setId(domain.getId());
        entity.setFlagName(domain.getFlagName());
        entity.setEnabled(domain.isEnabled());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    private FeatureFlag toDomain(FeatureFlagEntity entity) {
        var domain = new FeatureFlag();
        domain.setId(entity.getId());
        domain.setFlagName(entity.getFlagName());
        domain.setEnabled(entity.isEnabled());
        domain.setDescription(entity.getDescription());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
