package com.zayenha.qatra.appointment.infrastructure.persistence.repository;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long> {
    List<AppointmentEntity> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    List<AppointmentEntity> findByCenterIdAndAppointmentDateOrderByStartTime(Long centerId, LocalDate appointmentDate);
    Page<AppointmentEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    boolean existsByDonorIdAndStatusIn(Long donorId, List<AppointmentStatus> statuses);
}
