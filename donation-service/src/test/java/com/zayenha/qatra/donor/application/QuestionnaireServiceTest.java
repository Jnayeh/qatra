package com.zayenha.qatra.donor.application;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.port.in.QuestionnaireCommandUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra._shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    @Mock
    private DonorRepositoryPort donorRepository;

    private QuestionnaireService questionnaireService;

    @BeforeEach
    void setUp() {
        questionnaireService = new QuestionnaireService(donorRepository);
    }

    private DonorProfile aProfile() {
        var profile = new DonorProfile(1L);
        profile.setId(10L);
        return profile;
    }

    @Test
    void updateHealthQuestionnaireSetsRestrictionReasonForChronicIllness() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(donorRepository.saveQuestionnaire(any())).thenAnswer(i -> i.getArgument(0));

        var command = new QuestionnaireCommandUseCases.HealthQuestionnaireCommand(
                true, null, false, null, false, false, false);
        var result = questionnaireService.updateHealthQuestionnaire(1L, command);

        assertThat(result.isHasChronicIllness()).isTrue();
        assertThat(profile.getRestrictionReason()).contains("Chronic illness");
    }

    @Test
    void updateHealthQuestionnaireSetsRestrictionReasonForMedicationKeyword() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());
        when(donorRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(donorRepository.saveQuestionnaire(any())).thenAnswer(i -> i.getArgument(0));

        var command = new QuestionnaireCommandUseCases.HealthQuestionnaireCommand(
                false, null, true, "Patient takes insulin daily",
                false, false, false);
        var result = questionnaireService.updateHealthQuestionnaire(1L, command);

        assertThat(result.isOnMedication()).isTrue();
        assertThat(profile.getRestrictionReason()).contains("insulin");
    }

    @Test
    void getHealthQuestionnaireReturnsQuestionnaire() {
        var profile = aProfile();
        var q = new HealthQuestionnaire(10L);
        q.setId(100L);
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.of(q));

        var result = questionnaireService.getHealthQuestionnaire(1L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getHealthQuestionnaireThrowsWhenNotFound() {
        var profile = aProfile();
        when(donorRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(donorRepository.findQuestionnaireByDonorId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionnaireService.getHealthQuestionnaire(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Health questionnaire");
    }
}
