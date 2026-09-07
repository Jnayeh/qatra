package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra.emergency.infrastructure.persistence.entity.MatchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchResultJpaRepository extends JpaRepository<MatchResultEntity, Long> {
    List<MatchResultEntity> findByEmergency_Id(Long emergencyId);
    Optional<MatchResultEntity> findByEmergency_IdAndDonor_Id(Long emergencyId, Long donorId);
    List<MatchResultEntity> findByDonor_Id(Long donorId);
    long countByEmergency_Id(Long emergencyId);

    @Query(value = "SELECT COUNT(*) FROM match_results WHERE emergency_id = :eid AND status = 'PENDING'",
            nativeQuery = true)
    long countPendingByEmergencyId(@Param("eid") Long emergencyId);
}
