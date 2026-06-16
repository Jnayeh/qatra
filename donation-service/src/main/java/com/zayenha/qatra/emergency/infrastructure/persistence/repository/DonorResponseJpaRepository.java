package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DonorResponseJpaRepository extends JpaRepository<DonorResponseEntity, Long> {
    List<DonorResponseEntity> findByEmergency_IdOrderByCreatedAtAsc(Long emergencyId);
    List<DonorResponseEntity> findByDonor_IdOrderByCreatedAtDesc(Long donorId);
    boolean existsByEmergency_IdAndDonor_Id(Long emergencyId, Long donorId);

    @Query(value = "SELECT COUNT(*) FROM donor_responses dr " +
           "JOIN emergency_requests er ON dr.emergency_id = er.id " +
           "WHERE er.center_id = :cid AND dr.created_at >= :start AND dr.created_at < :end",
           nativeQuery = true)
    long countByCenterIdAndCreatedAtBetween(@org.springframework.data.repository.query.Param("cid") Long centerId,
                                             @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                                             @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);

    @Query(value = "SELECT dr.status, COUNT(*) FROM donor_responses dr " +
           "JOIN emergency_requests er ON dr.emergency_id = er.id " +
           "WHERE er.center_id = :cid AND dr.created_at >= :start AND dr.created_at < :end GROUP BY dr.status",
           nativeQuery = true)
    List<Object[]> countByCenterIdGroupedByStatus(@org.springframework.data.repository.query.Param("cid") Long centerId,
                                                   @org.springframework.data.repository.query.Param("start") java.time.LocalDateTime start,
                                                   @org.springframework.data.repository.query.Param("end") java.time.LocalDateTime end);
}
