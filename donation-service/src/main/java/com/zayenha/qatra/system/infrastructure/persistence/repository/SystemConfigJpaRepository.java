package com.zayenha.qatra.system.infrastructure.persistence.repository;

import com.zayenha.qatra.system.infrastructure.persistence.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, Long> {
    Optional<SystemConfigEntity> findByConfigKey(String configKey);
    void deleteByConfigKey(String configKey);
}
