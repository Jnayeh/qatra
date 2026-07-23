package com.zayenha.qatra.donor.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.infrastructure.mapper.DonorMapper;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.HealthQuestionnaireJpaRepository;
import com.zayenha.qatra.user.infrastructure.web.dto.response.RestrictedUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
    public Optional<HealthQuestionnaire> findQuestionnaireByUserId(Long userId) {
        return questionnaireJpaRepository.findByDonor_User_Id(userId).map(mapper::toQuestionnaireDomain);
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

    @Override
    public List<DonorProfile> findEligibilityRestoredDonors() {
        return donorJpaRepository.findDonorsWhoseEligibilityIsRestored().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<DonorProfile> findByEligibleFromDate(java.time.LocalDate date) {
        return donorJpaRepository.findByEligibleFromDate(date).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<DonorProfile> findIncompleteProfiles() {
        return donorJpaRepository.findIncompleteProfiles().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public PageResult<RestrictedUserResponse> findPermanentlyRestricted(int page, int size) {
        var pageable = PageRequest.of(page, size);
        var result = donorJpaRepository.findPermanentlyRestricted(pageable);
        return new PageResult<>(
                result.getContent().stream()
                        .map(e -> new RestrictedUserResponse(
                                e.getUser().getId(),
                                e.getUser().getEmail(),
                                e.getUser().getDisplayName(),
                                e.getUser().getStatus().name(),
                                e.getId(),
                                e.getPermanentlyRestricted(),
                                e.getRestrictionReason()))
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
