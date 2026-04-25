package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CenterStaffJpaRepository extends JpaRepository<CenterStaffProfileEntity, Long> {

    List<CenterStaffProfileEntity> findByCenter_Id(Long centerId);

    Optional<CenterStaffProfileEntity> findByCenter_IdAndUser_Id(Long centerId, Long userId);

    boolean existsByCenter_IdAndUser_Id(Long centerId, Long userId);

    void deleteByCenter_IdAndUser_Id(Long centerId, Long userId);
}
