package com.zayenha.qatra.analytics.infrastructure.persistence.repository;

import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
    List<AuditLogEntity> findByEventTypeOrderByTimestampDesc(String eventType);
    List<AuditLogEntity> findByActorIdOrderByTimestampDesc(Long actorId);
    List<AuditLogEntity> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);
    long countByEventType(String eventType);
}
