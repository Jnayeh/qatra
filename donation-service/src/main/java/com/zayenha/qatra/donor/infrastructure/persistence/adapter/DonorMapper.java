package com.zayenha.qatra.donor.infrastructure.persistence.adapter;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.HealthQuestionnaireEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import com.zayenha.qatra.user.infrastructure.persistence.repository.UserJpaRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DonorMapper {

    @Autowired
    protected UserJpaRepository userJpaRepository;

    @Autowired
    protected DonorJpaRepository donorJpaRepository;

    @Mapping(target = "user", expression = "java(userJpaRepository.getReferenceById(profile.getUserId()))")
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
