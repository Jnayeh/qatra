package com.zayenha.qatra.analytics.infrastructure.persistence.repository;

import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    List<AuditLogEntity> findByActionOrderByTimestampDesc(String action);

    List<AuditLogEntity> findByUser_IdOrderByTimestampDesc(Long userId);

    List<AuditLogEntity> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);

    @Query(value = "SELECT DATE(timestamp) as day, COUNT(*) as cnt FROM audit_logs al " +
           "WHERE al.action = :action AND al.timestamp >= :from AND al.timestamp < :to AND " +
           "(al.entity_id IN (SELECT id FROM emergency_requests WHERE center_id = :cid) OR " +
           "al.entity_id IN (SELECT id FROM appointments WHERE center_id = :cid)) " +
           "GROUP BY DATE(timestamp) ORDER BY day",
           nativeQuery = true)
    List<Object[]> countByCenterAndActionByDay(@Param("action") String action, @Param("cid") Long centerId,
                                               @Param("from") Instant from, @Param("to") Instant to);
}
