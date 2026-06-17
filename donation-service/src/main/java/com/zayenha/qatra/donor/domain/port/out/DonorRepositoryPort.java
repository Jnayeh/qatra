package com.zayenha.qatra.donor.domain.port.out;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

import java.util.List;
import java.util.Optional;

public interface DonorRepositoryPort {
    DonorProfile save(DonorProfile profile);
    Optional<DonorProfile> findByUserId(Long userId);
    Optional<DonorProfile> findById(Long id);
    boolean existsByUserId(Long userId);
    HealthQuestionnaire saveQuestionnaire(HealthQuestionnaire questionnaire);
    Optional<HealthQuestionnaire> findQuestionnaireByDonorId(Long donorId);
    Optional<HealthQuestionnaire> findQuestionnaireByUserId(Long userId);
    boolean donorHasQuestionnaire(Long donorId);
    void deleteByUserId(Long userId);
    List<DonorProfile> findEligibleForEmergency();
    List<DonorProfile> findEligibilityRestoredDonors();
    List<DonorProfile> findByEligibleFromDate(java.time.LocalDate date);

    List<DonorProfile> findIncompleteProfiles();
}
