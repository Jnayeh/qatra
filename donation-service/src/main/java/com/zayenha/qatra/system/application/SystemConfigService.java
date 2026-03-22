package com.zayenha.qatra.system.application;

import com.zayenha.qatra.system.domain.model.SystemConfig;
import com.zayenha.qatra.system.domain.port.out.SystemConfigRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SystemConfigService {

    private final SystemConfigRepositoryPort repository;

    @Transactional
    public SystemConfig set(String key, String value, String description) {
        var existing = repository.findByKey(key);
        if (existing.isPresent()) {
            var config = existing.get();
            config.setConfigValue(value);
            if (description != null) config.setDescription(description);
            return repository.save(config);
        }
        return repository.save(new SystemConfig(key, value, description));
    }

    @Transactional(readOnly = true)
    public Optional<SystemConfig> get(String key) {
        return repository.findByKey(key);
    }

    @Transactional(readOnly = true)
    public List<SystemConfig> getAll() {
        return repository.findAll();
    }

    @Transactional
    public void delete(String key) {
        repository.deleteByKey(key);
    }
}
