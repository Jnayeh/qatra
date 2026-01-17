package com.zayenha.qatra.appointment.adapter.out.persistence;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, Long> {
    List<AppointmentJpaEntity> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    @Query("SELECT a FROM AppointmentJpaEntity a WHERE a.centerId = :centerId AND a.createdAt BETWEEN :start AND :end ORDER BY a.createdAt ASC")
    List<AppointmentJpaEntity> findByCenterIdAndDateRange(@Param("centerId") Long centerId,
                                                           @Param("start") Instant start,
                                                           @Param("end") Instant end);

    long countByStatus(AppointmentStatus status);

    @Query("SELECT COALESCE(SUM(a.mlCollected), 0) FROM AppointmentJpaEntity a WHERE a.status = 'COMPLETED'")
    long totalMlCollected();

    @Query("SELECT COALESCE(SUM(a.mlCollected), 0) FROM AppointmentJpaEntity a WHERE a.status = 'COMPLETED' AND a.completedAt BETWEEN :start AND :end")
    long totalMlCollectedBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT COUNT(a) FROM AppointmentJpaEntity a WHERE a.status = 'COMPLETED' AND a.completedAt BETWEEN :start AND :end")
    long countCompletedBetween(@Param("start") Instant start, @Param("end") Instant end);

    long countByCenterIdAndStatus(Long centerId, AppointmentStatus status);

    @Query("SELECT a.centerId, COUNT(a), COALESCE(SUM(a.mlCollected), 0) FROM AppointmentJpaEntity a WHERE a.status = 'COMPLETED' GROUP BY a.centerId")
    List<Object[]> donationSummaryByCenter();

    @Query("""
            SELECT FUNCTION('DATE', a.completedAt), COUNT(a), COALESCE(SUM(a.mlCollected), 0) FROM AppointmentJpaEntity a
            WHERE a.status = 'COMPLETED' AND a.completedAt BETWEEN :start AND :end
            GROUP BY FUNCTION('DATE', a.completedAt) ORDER BY FUNCTION('DATE', a.completedAt)""")
    List<Object[]> dailyDonationStats(@Param("start") Instant start, @Param("end") Instant end);

    @Query("""
            SELECT a FROM AppointmentJpaEntity a
            WHERE a.centerId = COALESCE(:centerId, a.centerId)
            AND a.createdAt >= COALESCE(:from, a.createdAt)
            AND a.createdAt <= COALESCE(:to, a.createdAt)""")
    Page<AppointmentJpaEntity> findAllFiltered(@Param("centerId") Long centerId,
                                                @Param("from") Instant from,
                                                @Param("to") Instant to,
                                                Pageable pageable);

    @Query("""
            SELECT COUNT(a) FROM AppointmentJpaEntity a
            WHERE (:centerId IS NULL OR a.centerId = :centerId)
            AND (:from IS NULL OR a.createdAt >= :from)
            AND (:to IS NULL OR a.createdAt <= :to)""")
    long countAllFiltered(@Param("centerId") Long centerId,
                          @Param("from") Instant from,
                          @Param("to") Instant to);
}
