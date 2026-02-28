package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CenterStaffJpaRepository extends JpaRepository<CenterStaffProfileEntity, Long> {

    List<CenterStaffProfileEntity> findByCenterId(Long centerId);

    Optional<CenterStaffProfileEntity> findByCenterIdAndUserId(Long centerId, Long userId);

    boolean existsByCenterIdAndUserId(Long centerId, Long userId);

    void deleteByCenterIdAndUserId(Long centerId, Long userId);
}
