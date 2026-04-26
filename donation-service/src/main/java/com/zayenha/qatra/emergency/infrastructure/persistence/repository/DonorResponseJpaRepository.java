package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra.emergency.infrastructure.persistence.entity.DonorResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonorResponseJpaRepository extends JpaRepository<DonorResponseEntity, Long> {
    List<DonorResponseEntity> findByEmergency_IdOrderByCreatedAtAsc(Long emergencyId);
    List<DonorResponseEntity> findByDonor_IdOrderByCreatedAtDesc(Long donorId);
    boolean existsByEmergency_IdAndDonor_Id(Long emergencyId, Long donorId);
}
