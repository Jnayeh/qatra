package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmergencyJpaRepository extends JpaRepository<EmergencyRequestEntity, Long> {
    Page<EmergencyRequestEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<EmergencyRequestEntity> findByBloodTypeAndStatus(BloodType bloodType, EmergencyStatus status);
    List<EmergencyRequestEntity> findByStatus(EmergencyStatus status);

    @Query(value = "SELECT COUNT(*) FROM emergency_requests WHERE center_id = :cid AND status = 'OPEN'",
            nativeQuery = true)
    long countActiveByCenterId(@Param("cid") Long centerId);

    @Query(value = "SELECT COUNT(*) FROM emergency_requests WHERE center_id = :cid AND created_at >= :start AND created_at < :end",
            nativeQuery = true)
    long countByCenterIdAndCreatedAtBetween(@Param("cid") Long centerId, @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);

    @Query(value = "SELECT status, COUNT(*) FROM emergency_requests WHERE center_id = :cid AND created_at >= :start AND created_at < :end GROUP BY status",
            nativeQuery = true)
    List<Object[]> countByCenterIdGroupedByStatus(@Param("cid") Long centerId, @Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}
