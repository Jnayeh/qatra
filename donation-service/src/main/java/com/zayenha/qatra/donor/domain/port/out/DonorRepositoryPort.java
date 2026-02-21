package com.zayenha.qatra.donor.domain.port.out;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

import java.util.Optional;

public interface DonorRepositoryPort {
    DonorProfile save(DonorProfile profile);
    Optional<DonorProfile> findByUserId(Long userId);
    Optional<DonorProfile> findById(Long id);
    boolean existsByUserId(Long userId);
    HealthQuestionnaire saveQuestionnaire(HealthQuestionnaire questionnaire);
    Optional<HealthQuestionnaire> findQuestionnaireByDonorId(Long donorId);
    boolean donorHasQuestionnaire(Long donorId);
    void deleteByUserId(Long userId);
}
