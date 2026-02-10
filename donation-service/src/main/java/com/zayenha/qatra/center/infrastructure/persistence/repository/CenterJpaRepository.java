package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CenterJpaRepository extends JpaRepository<CenterEntity, Long>, JpaSpecificationExecutor<CenterEntity> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}
