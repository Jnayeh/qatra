package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DonorJpaRepository extends JpaRepository<DonorProfileEntity, Long> {
    Optional<DonorProfileEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
