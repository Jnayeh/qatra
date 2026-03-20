package com.zayenha.qatra.system.infrastructure.persistence.adapter;

import com.zayenha.qatra.system.domain.model.SystemConfig;
import com.zayenha.qatra.system.domain.port.out.SystemConfigRepositoryPort;
import com.zayenha.qatra.system.infrastructure.persistence.entity.SystemConfigEntity;
import com.zayenha.qatra.system.infrastructure.persistence.repository.SystemConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SystemConfigRepositoryAdapter implements SystemConfigRepositoryPort {

    private final SystemConfigJpaRepository jpaRepository;

    @Override
    public SystemConfig save(SystemConfig config) {
        var entity = toEntity(config);
        if (entity.getId() != null) {
            var existing = jpaRepository.findById(entity.getId()).orElseThrow();
            existing.setConfigValue(entity.getConfigValue());
            existing.setDescription(entity.getDescription());
            return toDomain(jpaRepository.save(existing));
        }
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<SystemConfig> findByKey(String key) {
        return jpaRepository.findByConfigKey(key).map(this::toDomain);
    }

    @Override
    public List<SystemConfig> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteByKey(String key) {
        jpaRepository.deleteByConfigKey(key);
    }

    private SystemConfigEntity toEntity(SystemConfig domain) {
        var entity = new SystemConfigEntity();
        entity.setId(domain.getId());
        entity.setConfigKey(domain.getConfigKey());
        entity.setConfigValue(domain.getConfigValue());
        entity.setDescription(domain.getDescription());
        return entity;
    }

    private SystemConfig toDomain(SystemConfigEntity entity) {
        var domain = new SystemConfig();
        domain.setId(entity.getId());
        domain.setConfigKey(entity.getConfigKey());
        domain.setConfigValue(entity.getConfigValue());
        domain.setDescription(entity.getDescription());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
