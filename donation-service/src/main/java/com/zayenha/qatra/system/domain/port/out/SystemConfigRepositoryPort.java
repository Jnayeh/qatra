package com.zayenha.qatra.system.domain.port.out;

import com.zayenha.qatra.system.domain.model.SystemConfig;

import java.util.List;
import java.util.Optional;

public interface SystemConfigRepositoryPort {
    SystemConfig save(SystemConfig config);
    Optional<SystemConfig> findByKey(String key);
    List<SystemConfig> findAll();
    void deleteByKey(String key);
}
