package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CenterJpaRepository extends JpaRepository<CenterEntity, Long>, JpaSpecificationExecutor<CenterEntity> {
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    Page<CenterEntity> findByStatus(CenterStatus status, Pageable pageable);
    List<CenterEntity> findAllByStatus(CenterStatus status);
    long countByStatus(CenterStatus status);

    @EntityGraph("CenterEntity.withSlots")
    Optional<CenterEntity> findWithSlotsById(Long id);
}
