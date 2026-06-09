package com.zayenha.qatra.analytics.infrastructure.persistence.repository;

import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, Long> {
    Page<AuditLogEntity> findAllByOrderByTimestampDesc(Pageable pageable);
    List<AuditLogEntity> findByActionOrderByTimestampDesc(String action);
    List<AuditLogEntity> findByUser_IdOrderByTimestampDesc(Long userId);
    List<AuditLogEntity> findByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);
    long countByAction(String action);
    long countByActionAndTimestampBetween(String action, Instant from, Instant to);

    @Query("SELECT a FROM AuditLogEntity a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:fromDate IS NULL OR a.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR a.timestamp <= :toDate) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLogEntity> findFiltered(@Param("action") String action,
                                       @Param("fromDate") Instant fromDate,
                                       @Param("toDate") Instant toDate,
                                       Pageable pageable);

    @Query("SELECT COUNT(a) FROM AuditLogEntity a WHERE " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:fromDate IS NULL OR a.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR a.timestamp <= :toDate)")
    long countFiltered(@Param("action") String action,
                       @Param("fromDate") Instant fromDate,
                       @Param("toDate") Instant toDate);
}
