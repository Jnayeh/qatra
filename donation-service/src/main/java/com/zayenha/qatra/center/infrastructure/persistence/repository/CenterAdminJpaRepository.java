package com.zayenha.qatra.center.infrastructure.persistence.repository;

import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterAdminProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CenterAdminJpaRepository extends JpaRepository<CenterAdminProfileEntity, Long> {

    Optional<CenterAdminProfileEntity> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);

    List<CenterAdminProfileEntity> findByCenter_Id(Long centerId);
}
