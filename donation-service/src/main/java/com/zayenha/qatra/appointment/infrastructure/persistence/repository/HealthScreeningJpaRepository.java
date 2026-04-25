package com.zayenha.qatra.appointment.infrastructure.persistence.repository;

import com.zayenha.qatra.appointment.infrastructure.persistence.entity.HealthScreeningEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthScreeningJpaRepository extends JpaRepository<HealthScreeningEntity, Long> {
    Optional<HealthScreeningEntity> findByAppointment_Id(Long appointmentId);
}
