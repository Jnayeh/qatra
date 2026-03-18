package com.zayenha.qatra.system.infrastructure.persistence.repository;

import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.infrastructure.persistence.entity.GDPRDeletionRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GDPRDeletionRequestJpaRepository extends JpaRepository<GDPRDeletionRequestEntity, Long> {
    Optional<GDPRDeletionRequestEntity> findByUserId(Long userId);
    List<GDPRDeletionRequestEntity> findByStatus(GDPRDeletionStatus status);
}
