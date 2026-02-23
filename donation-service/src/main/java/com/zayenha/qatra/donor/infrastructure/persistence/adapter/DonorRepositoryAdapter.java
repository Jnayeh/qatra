package com.zayenha.qatra.donor.infrastructure.persistence.adapter;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.HealthQuestionnaireEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.HealthQuestionnaireJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DonorRepositoryAdapter implements DonorRepositoryPort {

    private final DonorJpaRepository donorJpaRepository;
    private final HealthQuestionnaireJpaRepository questionnaireJpaRepository;

    @Override
    public DonorProfile save(DonorProfile profile) {
        var entity = toEntity(profile);
        var saved = donorJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<DonorProfile> findByUserId(Long userId) {
        return donorJpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<DonorProfile> findById(Long id) {
        return donorJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return donorJpaRepository.existsByUserId(userId);
    }

    @Override
    public HealthQuestionnaire saveQuestionnaire(HealthQuestionnaire questionnaire) {
        var entity = toQuestionnaireEntity(questionnaire);
        var saved = questionnaireJpaRepository.save(entity);
        return toQuestionnaireDomain(saved);
    }

    @Override
    public Optional<HealthQuestionnaire> findQuestionnaireByDonorId(Long donorId) {
        return questionnaireJpaRepository.findByDonorId(donorId).map(this::toQuestionnaireDomain);
    }
    @Override
    public boolean donorHasQuestionnaire(Long donorId) {
        return questionnaireJpaRepository.existsByDonorId(donorId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        donorJpaRepository.findByUserId(userId).ifPresent(donorJpaRepository::delete);
    }

    private DonorProfileEntity toEntity(DonorProfile profile) {
        var entity = new DonorProfileEntity();
        entity.setId(profile.getId());
        entity.setUserId(profile.getUserId());
        entity.setBloodType(profile.getBloodType());
        entity.setBloodTypeVerified(profile.isBloodTypeVerified());
        entity.setLatitude(profile.getLatitude());
        entity.setLongitude(profile.getLongitude());
        entity.setCity(profile.getCity());
        entity.setCountry(profile.getCountry());
        entity.setAvailabilityStatus(profile.getAvailabilityStatus());
        entity.setNotificationPreferences(profile.getNotificationPreferences());
        entity.setPermanentlyRestricted(profile.isPermanentlyRestricted());
        entity.setRestrictionReason(profile.getRestrictionReason());
        entity.setFlaggedForManualReview(profile.isFlaggedForManualReview());
        entity.setConsecutiveEmergencyDeclines(profile.getConsecutiveEmergencyDeclines());
        entity.setReliabilityScore(profile.getReliabilityScore());
        entity.setEligibleFromDate(profile.getEligibleFromDate());
        entity.setProfileComplete(profile.isProfileComplete());
        entity.setLastAcceptAt(profile.getLastAcceptAt());
        entity.setTotalDonations(profile.getTotalDonations());
        entity.setEstimatedLivesSaved(profile.getEstimatedLivesSaved());
        entity.setStatus(profile.getStatus());
        entity.setCreatedAt(profile.getCreatedAt());
        entity.setUpdatedAt(profile.getUpdatedAt());
        return entity;
    }

    private DonorProfile toDomain(DonorProfileEntity entity) {
        var profile = new DonorProfile();
        profile.setId(entity.getId());
        profile.setUserId(entity.getUserId());
        profile.setBloodType(entity.getBloodType());
        profile.setBloodTypeVerified(entity.isBloodTypeVerified());
        profile.setLatitude(entity.getLatitude());
        profile.setLongitude(entity.getLongitude());
        profile.setCity(entity.getCity());
        profile.setCountry(entity.getCountry());
        profile.setAvailabilityStatus(entity.getAvailabilityStatus());
        profile.setNotificationPreferences(entity.getNotificationPreferences());
        profile.setPermanentlyRestricted(entity.isPermanentlyRestricted());
        profile.setRestrictionReason(entity.getRestrictionReason());
        profile.setFlaggedForManualReview(entity.isFlaggedForManualReview());
        profile.setConsecutiveEmergencyDeclines(entity.getConsecutiveEmergencyDeclines());
        profile.setReliabilityScore(entity.getReliabilityScore());
        profile.setEligibleFromDate(entity.getEligibleFromDate());
        profile.setProfileComplete(entity.isProfileComplete());
        profile.setLastAcceptAt(entity.getLastAcceptAt());
        profile.setTotalDonations(entity.getTotalDonations());
        profile.setEstimatedLivesSaved(entity.getEstimatedLivesSaved());
        profile.setStatus(entity.getStatus());
        profile.setCreatedAt(entity.getCreatedAt());
        profile.setUpdatedAt(entity.getUpdatedAt());
        return profile;
    }

    private HealthQuestionnaireEntity toQuestionnaireEntity(HealthQuestionnaire questionnaire) {
        var entity = new HealthQuestionnaireEntity();
        entity.setId(questionnaire.getId());
        entity.setDonorId(questionnaire.getDonorId());
        entity.setHasChronicIllness(questionnaire.isHasChronicIllness());
        entity.setMedicalConditionsDetails(questionnaire.getMedicalConditionsDetails());
        entity.setOnMedication(questionnaire.isOnMedication());
        entity.setMedicationDetails(questionnaire.getMedicationDetails());
        entity.setRecentSurgery(questionnaire.isRecentSurgery());
        entity.setRecentTravel(questionnaire.isRecentTravel());
        entity.setRecentTattooOrPiercing(questionnaire.isRecentTattooOrPiercing());
        entity.setCreatedAt(questionnaire.getCreatedAt());
        entity.setUpdatedAt(questionnaire.getUpdatedAt());
        return entity;
    }

    private HealthQuestionnaire toQuestionnaireDomain(HealthQuestionnaireEntity entity) {
        var questionnaire = new HealthQuestionnaire();
        questionnaire.setId(entity.getId());
        questionnaire.setDonorId(entity.getDonorId());
        questionnaire.setHasChronicIllness(entity.isHasChronicIllness());
        questionnaire.setMedicalConditionsDetails(entity.getMedicalConditionsDetails());
        questionnaire.setOnMedication(entity.isOnMedication());
        questionnaire.setMedicationDetails(entity.getMedicationDetails());
        questionnaire.setRecentSurgery(entity.isRecentSurgery());
        questionnaire.setRecentTravel(entity.isRecentTravel());
        questionnaire.setRecentTattooOrPiercing(entity.isRecentTattooOrPiercing());
        questionnaire.setCreatedAt(entity.getCreatedAt());
        questionnaire.setUpdatedAt(entity.getUpdatedAt());
        return questionnaire;
    }
}
