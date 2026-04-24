package com.zayenha.qatra.donor.infrastructure.persistence.adapter;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.HealthQuestionnaireJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DonorRepositoryAdapter implements DonorRepositoryPort {

    private final DonorJpaRepository donorJpaRepository;
    private final HealthQuestionnaireJpaRepository questionnaireJpaRepository;
    private final DonorMapper mapper;

    @Override
    public DonorProfile save(DonorProfile profile) {
        var entity = mapper.toEntity(profile);
        var saved = donorJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<DonorProfile> findByUserId(Long userId) {
        return donorJpaRepository.findByUser_Id(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<DonorProfile> findById(Long id) {
        return donorJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsByUserId(Long userId) {
        return donorJpaRepository.existsByUser_Id(userId);
    }

    @Override
    public HealthQuestionnaire saveQuestionnaire(HealthQuestionnaire questionnaire) {
        var entity = mapper.toQuestionnaireEntity(questionnaire);
        var saved = questionnaireJpaRepository.save(entity);
        return mapper.toQuestionnaireDomain(saved);
    }

    @Override
    public Optional<HealthQuestionnaire> findQuestionnaireByDonorId(Long donorId) {
        return questionnaireJpaRepository.findByDonor_Id(donorId).map(mapper::toQuestionnaireDomain);
    }

    @Override
    public boolean donorHasQuestionnaire(Long donorId) {
        return questionnaireJpaRepository.existsByDonor_Id(donorId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        donorJpaRepository.findByUser_Id(userId).ifPresent(donorJpaRepository::delete);
    }

    @Override
    public List<DonorProfile> findEligibleForEmergency() {
        return donorJpaRepository.findEligibleForEmergency().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
