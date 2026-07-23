package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
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

    @Query("SELECT d FROM DonorProfileEntity d WHERE d.eligibleFromDate IS NOT NULL AND d.eligibleFromDate <= CURRENT_DATE AND d.permanentlyRestricted = false AND d.status = 'ACTIVE'")
    List<DonorProfileEntity> findDonorsWhoseEligibilityIsRestored();

    @Query("SELECT d FROM DonorProfileEntity d WHERE d.eligibleFromDate = :date AND d.permanentlyRestricted = false AND d.status = 'ACTIVE'")
    List<DonorProfileEntity> findByEligibleFromDate(@Param("date") LocalDate date);

    @Query("SELECT d FROM DonorProfileEntity d WHERE d.profileComplete = false AND d.status = 'ACTIVE' AND d.deletedAt IS NULL")
    List<DonorProfileEntity> findIncompleteProfiles();

    @Query(value = "SELECT d FROM DonorProfileEntity d JOIN FETCH d.user WHERE d.permanentlyRestricted = true AND d.deletedAt IS NULL",
           countQuery = "SELECT COUNT(d) FROM DonorProfileEntity d WHERE d.permanentlyRestricted = true AND d.deletedAt IS NULL")
    Page<DonorProfileEntity> findPermanentlyRestricted(Pageable pageable);
}
