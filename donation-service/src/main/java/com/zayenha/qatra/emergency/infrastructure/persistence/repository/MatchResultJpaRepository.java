package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra.emergency.infrastructure.persistence.entity.MatchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchResultJpaRepository extends JpaRepository<MatchResultEntity, Long> {
    List<MatchResultEntity> findByEmergency_Id(Long emergencyId);
    Optional<MatchResultEntity> findByEmergency_IdAndDonor_Id(Long emergencyId, Long donorId);
    List<MatchResultEntity> findByDonor_Id(Long donorId);
}
