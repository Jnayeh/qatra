package com.zayenha.qatra.appointment.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.DonationOutcome;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {
    @EntityGraph("UserEntity.withRoles")
    Page<UserEntity> findAll(Specification<UserEntity> spec, Pageable pageable);
    @EntityGraph("AppointmentEntity.withSlot")
    Optional<AppointmentEntity> findById(Long id);
    @EntityGraph("AppointmentEntity.withSlot")
    List<AppointmentEntity> findByDonor_IdOrderByCreatedAtDesc(Long donorId);
    @EntityGraph("AppointmentEntity.withSlot")
    Page<AppointmentEntity> findByDonor_IdOrderByCreatedAtDesc(Long donorId, Pageable pageable);
    @EntityGraph("AppointmentEntity.withSlot")
    Page<AppointmentEntity> findByDonor_IdAndStatusInOrderByCreatedAtDesc(Long donorId, List<AppointmentStatus> statuses, Pageable pageable);
    List<AppointmentEntity> findByCenter_IdOrderByCreatedAtDesc(Long centerId);
    @EntityGraph("AppointmentEntity.withSlot")
    Page<AppointmentEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    boolean existsByDonor_IdAndStatusIn(Long donorId, List<AppointmentStatus> statuses);
    @EntityGraph("AppointmentEntity.withSlot")
    List<AppointmentEntity> findByEmergency_Id(Long emergencyId);

    @Query("SELECT COUNT(a) FROM AppointmentEntity a WHERE a.emergency.id = :emergencyId AND a.outcome = :outcome AND a.status = :status")
    long countCompletedByEmergencyId(Long emergencyId, DonationOutcome outcome, AppointmentStatus status);

    @Query("SELECT a FROM AppointmentEntity a JOIN FETCH a.slot s WHERE a.status = 'SCHEDULED' AND s.date = :targetDate")
    List<AppointmentEntity> findScheduledAppointmentsBySlotDate(@Param("targetDate") java.time.LocalDate targetDate);

    @Query("SELECT a FROM AppointmentEntity a JOIN FETCH a.slot s WHERE a.center.id = :centerId AND s.date = :targetDate ORDER BY s.startTime ASC")
    List<AppointmentEntity> findByCenterIdAndSlotDate(@Param("centerId") Long centerId, @Param("targetDate") java.time.LocalDate targetDate);

    @Query(value = "SELECT a FROM AppointmentEntity a JOIN FETCH a.slot s JOIN FETCH a.donor d WHERE a.center.id = :centerId AND s.date BETWEEN :fromDate AND :toDate ORDER BY s.date ASC, s.startTime ASC",
            countQuery = "SELECT COUNT(a) FROM AppointmentEntity a JOIN a.slot s WHERE a.center.id = :centerId AND s.date BETWEEN :fromDate AND :toDate")
    Page<AppointmentEntity> findByCenterIdAndSlotDateBetween(@Param("centerId") Long centerId, @Param("fromDate") java.time.LocalDate fromDate, @Param("toDate") java.time.LocalDate toDate, Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(ml_collected), 0) FROM appointments WHERE center_id = :cid AND ml_collected IS NOT NULL",
            nativeQuery = true)
    long sumMlCollectedByCenterId(@Param("cid") Long centerId);

    @Query(value = "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND (:status IS NULL OR status = :status) AND created_at >= :start AND created_at < :end",
            nativeQuery = true)
    long countByCenterIdAndCreatedAtBetween(@Param("cid") Long centerId,
                                             @Param("status") String status,
                                             @Param("start") java.time.LocalDateTime start,
                                             @Param("end") java.time.LocalDateTime end);

    @Query(value = "SELECT outcome, COUNT(*) FROM appointments WHERE center_id = :cid AND outcome IS NOT NULL AND created_at >= :start AND created_at < :end GROUP BY outcome",
            nativeQuery = true)
    List<Object[]> countByCenterIdGroupedByOutcome(@Param("cid") Long centerId,
                                                    @Param("start") java.time.LocalDateTime start,
                                                    @Param("end") java.time.LocalDateTime end);

    @Query(value = "SELECT COALESCE(SUM(ml_collected), 0) FROM appointments WHERE center_id = :cid AND ml_collected IS NOT NULL AND created_at >= :start AND created_at < :end",
            nativeQuery = true)
    long sumMlCollectedByCenterIdAndCreatedAtBetween(@Param("cid") Long centerId,
                                                      @Param("start") java.time.LocalDateTime start,
                                                      @Param("end") java.time.LocalDateTime end);

    // --- Center dashboard metric counts (sourced from the appointments module, not the audit log) ---

    @Query(value = "SELECT COUNT(*) FROM appointments WHERE center_id = :cid",
            nativeQuery = true)
    long countByCenterId(@Param("cid") Long centerId);

    @Query(value = "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND status = :status",
            nativeQuery = true)
    long countByCenterIdAndStatus(@Param("cid") Long centerId, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND created_at >= :start AND created_at < :end",
            nativeQuery = true)
    long countByCenterIdInPeriod(@Param("cid") Long centerId,
                                  @Param("start") Instant start,
                                  @Param("end") Instant end);

    @Query(value = "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND appointment_type = :type AND created_at >= :start AND created_at < :end",
            nativeQuery = true)
    long countByCenterIdAndTypeInPeriod(@Param("cid") Long centerId,
                                         @Param("type") String type,
                                         @Param("start") Instant start,
                                         @Param("end") Instant end);

    @Query(value = "SELECT TO_CHAR(created_at AT TIME ZONE 'UTC', 'YYYY-MM-DD') AS day, COUNT(*) "
            + "FROM appointments WHERE center_id = :cid AND created_at >= :start AND created_at < :end "
            + "GROUP BY 1 ORDER BY 1",
            nativeQuery = true)
    List<Object[]> countByCenterIdGroupedByDay(@Param("cid") Long centerId,
                                                @Param("start") Instant start,
                                                @Param("end") Instant end);
}
