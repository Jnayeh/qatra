package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmergencyJpaRepository extends JpaRepository<EmergencyRequestEntity, Long> {
    Page<EmergencyRequestEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<EmergencyRequestEntity> findByBloodTypeAndStatus(BloodType bloodType, EmergencyStatus status);
}
