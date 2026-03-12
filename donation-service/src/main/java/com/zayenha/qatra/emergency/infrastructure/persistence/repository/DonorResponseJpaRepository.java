package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonorResponseJpaRepository extends JpaRepository<DonorResponseEntity, Long> {
    List<DonorResponseEntity> findByEmergencyIdOrderByCreatedAtAsc(Long emergencyId);
    List<DonorResponseEntity> findByDonorIdOrderByCreatedAtDesc(Long donorId);
    boolean existsByEmergencyIdAndDonorId(Long emergencyId, Long donorId);
}
