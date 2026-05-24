package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.HealthQuestionnaireEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HealthQuestionnaireJpaRepository extends JpaRepository<HealthQuestionnaireEntity, Long> {
    Optional<HealthQuestionnaireEntity> findByDonor_Id(Long donorId);
    Optional<HealthQuestionnaireEntity> findByDonor_User_Id(Long donorId);
    boolean existsByDonor_Id(Long donorId);
}
