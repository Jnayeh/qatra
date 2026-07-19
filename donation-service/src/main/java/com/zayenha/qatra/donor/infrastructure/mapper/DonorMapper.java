package com.zayenha.qatra.donor.infrastructure.mapper;

import com.zayenha.qatra.donor.application.proxy.DonorUserProxy;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.HealthQuestionnaireEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.donor.infrastructure.web.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class DonorMapper {

    @Autowired protected DonorUserProxy userProxy;

    @Autowired protected DonorJpaRepository donorJpaRepository;

    public abstract DonorProfileResponse toProfileResponse(DonorProfile profile);

    public abstract DonorHealthResponse toHealthResponse(HealthQuestionnaire questionnaire);

    public abstract ImpactResponse toImpactResponse(DonorQueryUseCases.ImpactResult result);

    public EligibilityResponse toEligibilityResponse(DonorProfile profile) {
        var now = Instant.now();
        var eligible = profile.getEligibleFromDate() == null
            || !now.isBefore(profile.getEligibleFromDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        String reason = null;
        if (!eligible) {
            reason = "Cooldown period active until " + profile.getEligibleFromDate();
        }
        if (Boolean.TRUE.equals(profile.getPermanentlyRestricted())) {
            eligible = false;
            reason = profile.getRestrictionReason();
        }
        return new EligibilityResponse(eligible, profile.getEligibleFromDate(), reason);
    }

    public EligibilityDetailResponse toEligibilityDetailResponse(DonorProfile profile) {
        var now = Instant.now();
        var eligible = profile.getEligibleFromDate() == null
            || !now.isBefore(profile.getEligibleFromDate().atStartOfDay(ZoneOffset.UTC).toInstant());
        if (Boolean.TRUE.equals(profile.getPermanentlyRestricted())) {
            eligible = false;
        }
        return new EligibilityDetailResponse(eligible, profile.getEligibleFromDate(),
            profile.getPermanentlyRestricted(), profile.getRestrictionReason());
    }

    public DonorDetailResponse toDetailResponse(DonorProfile profile, HealthQuestionnaire questionnaire) {
        var healthResponse = questionnaire != null ? toHealthResponse(questionnaire) : null;
        return new DonorDetailResponse(
            profile.getId(), profile.getUserId(),
            profile.getBloodType(), profile.getBloodTypeVerified(),
            profile.getLatitude(), profile.getLongitude(), profile.getCity(),
            profile.getAvailability(), profile.getNotificationPreferences(),
            profile.getPermanentlyRestricted(), profile.getRestrictionReason(),
            profile.getFlaggedForManualReview(), profile.getReliabilityScore(),
            profile.getEligibleFromDate(), profile.getProfileComplete(),
            profile.getTotalDonations(),
            healthResponse,
            profile.getCreatedAt(), profile.getUpdatedAt()
        );
    }

    @Mapping(target = "user", expression = "java(userProxy.getUserReference(profile.getUserId()))")
    public abstract DonorProfileEntity toEntity(DonorProfile profile);

    @Mapping(target = "userId", source = "user.id")
    public abstract DonorProfile toDomain(DonorProfileEntity entity);

    @Mapping(target = "donor", expression = "java(donorJpaRepository.getReferenceById(questionnaire.getDonorId()))")
    @Mapping(target = "hasChronicIllness", source = "hasChronicIllness")
    @Mapping(target = "onMedication", source = "onMedication")
    public abstract HealthQuestionnaireEntity toQuestionnaireEntity(HealthQuestionnaire questionnaire);

    @Mapping(target = "donorId", source = "donor.id")
    public abstract HealthQuestionnaire toQuestionnaireDomain(HealthQuestionnaireEntity entity);
}
