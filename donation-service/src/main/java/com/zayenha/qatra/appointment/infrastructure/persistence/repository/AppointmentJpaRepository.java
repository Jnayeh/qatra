package com.zayenha.qatra.appointment.infrastructure.persistence.repository;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByDonor_IdOrderByCreatedAtDesc(Long donorId);
    List<AppointmentEntity> findByCenter_IdOrderByCreatedAtDesc(Long centerId);
    Page<AppointmentEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    boolean existsByDonor_IdAndStatusIn(Long donorId, List<AppointmentStatus> statuses);
    List<AppointmentEntity> findByEmergency_Id(Long emergencyId);

    @Query("SELECT COUNT(a) FROM AppointmentEntity a WHERE a.emergency.id = :emergencyId AND a.outcome = :outcome AND a.status = :status")
    long countCompletedByEmergencyId(Long emergencyId, DonationOutcome outcome, AppointmentStatus status);
}
