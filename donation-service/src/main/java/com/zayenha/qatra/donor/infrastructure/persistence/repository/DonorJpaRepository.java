package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DonorJpaRepository extends JpaRepository<DonorProfileEntity, Long> {
    Optional<DonorProfileEntity> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);

    @Query("""
        SELECT d FROM DonorProfileEntity d
        WHERE d.status = 'ACTIVE'
          AND d.availability = 'AVAILABLE'
          AND d.permanentlyRestricted = false
          AND d.bloodType != 'UNKNOWN'
          AND d.latitude IS NOT NULL
          AND d.longitude IS NOT NULL
          AND d.allowEmergencyNotifications = true
          AND (d.eligibleFromDate IS NULL OR d.eligibleFromDate <= CURRENT_DATE)
        """)
    List<DonorProfileEntity> findEligibleForEmergency();
}
