package com.zayenha.qatra.donor.application;

import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.domain.service.DonorDomainValidator;
import com.zayenha.qatra.shared.domain.BloodType;
import com.zayenha.qatra.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DonorService implements DonorCommandUseCases, DonorQueryUseCases {

    private final DonorRepositoryPort donorRepository;

    private DonorDomainValidator validator() {
        return new DonorDomainValidator();
    }

    @Override
    @Transactional
    public DonorProfile updateProfile(Long userId, UpdateProfileCommand command) {
        var profile = donorRepository.findByUserId(userId).orElseGet(() -> {
            var newProfile = new DonorProfile(userId);
            return donorRepository.save(newProfile);
        });
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateBloodType(Long userId, BloodType bloodType) {
        validator().validateBloodType(bloodType);
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        validator().validateBloodTypeUpdate(profile);
        profile.setBloodType(bloodType);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateBloodTypeAdmin(Long donorId, BloodType bloodType) {
        validator().validateBloodType(bloodType);
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setBloodType(bloodType);
        profile.setBloodTypeVerified(true);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateLocation(Long userId, UpdateLocationCommand command) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setLatitude(command.latitude());
        profile.setLongitude(command.longitude());
        profile.setCity(command.city());
        profile.setCountry(command.country());
        profile.setUpdatedAt(Instant.now());
        checkProfileComplete(profile, null);
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateAvailability(Long userId, AvailabilityStatus status) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setAvailabilityStatus(status);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateNotificationPrefs(Long userId, NotificationPreferences prefs) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setNotificationPreferences(prefs);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public HealthQuestionnaire updateHealthQuestionnaire(Long userId, HealthQuestionnaireCommand command) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));

        var questionnaire = donorRepository.findQuestionnaireByDonorId(profile.getId())
                .orElseGet(() -> new HealthQuestionnaire(profile.getId()));

        questionnaire.setHasChronicIllness(command.hasChronicIllness());
        questionnaire.setMedicalConditionsDetails(command.medicalConditionsDetails());
        questionnaire.setOnMedication(command.onMedication());
        questionnaire.setMedicationDetails(command.medicationDetails());
        questionnaire.setRecentSurgery(command.recentSurgery());
        questionnaire.setRecentTravel(command.recentTravel());
        questionnaire.setRecentTattooOrPiercing(command.recentTattooOrPiercing());
        questionnaire.setUpdatedAt(Instant.now());

        evaluatePermanentRestriction(profile, command);
        profile.setUpdatedAt(Instant.now());
        checkProfileComplete(profile, questionnaire);

        donorRepository.save(profile);
        return donorRepository.saveQuestionnaire(questionnaire);
    }

    @Override
    @Transactional
    public void requestDeletion(Long userId) {
        var profile = donorRepository.findByUserId(userId).orElseGet(() ->
                donorRepository.save(new DonorProfile(userId)));
        profile.setStatus(DonorStatus.INACTIVE);
        profile.setUpdatedAt(Instant.now());
        donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateRestriction(Long donorId, boolean permanentlyRestricted, String reason) {
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setPermanentlyRestricted(permanentlyRestricted);
        profile.setRestrictionReason(reason);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional
    public DonorProfile updateFlag(Long donorId, boolean flagged) {
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setFlaggedForManualReview(flagged);
        profile.setUpdatedAt(Instant.now());
        return donorRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfile getMyProfile(Long userId) {
        return donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfile getDonorById(Long donorId) {
        return donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
    }

    @Override
    @Transactional
    public HealthQuestionnaire getHealthQuestionnaire(Long userId) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        return donorRepository.findQuestionnaireByDonorId(profile.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Health questionnaire not found",
                        DonorErrorCode.HEALTH_QUESTIONNAIRE_NOT_FOUND.name()));
    }

    private void evaluatePermanentRestriction(DonorProfile profile, HealthQuestionnaireCommand command) {
        if (command.hasChronicIllness()) {
            profile.setRestrictionReason("Chronic illness indicated in health questionnaire");
        } else if (command.onMedication() && command.medicationDetails() != null
                && containsPermanentMedicationKeyword(command.medicationDetails())) {
            profile.setRestrictionReason("Permanent medication: " + command.medicationDetails());
        }
    }

    private boolean containsPermanentMedicationKeyword(String details) {
        var keywords = List.of("insulin", "chemo", "immunosuppressant");
        var lower = details.toLowerCase();
        return keywords.stream().anyMatch(lower::contains);
    }

    private void checkProfileComplete(DonorProfile profile, HealthQuestionnaire questionnaire) {
        boolean hasLocation = profile.getLatitude() != null && profile.getLongitude() != null;
        boolean hasQuestionnaire = hasQuestionnaire(profile.getId(), questionnaire);
        profile.setProfileComplete(hasLocation && hasQuestionnaire);
    }
    private boolean hasQuestionnaire(Long id, HealthQuestionnaire questionnaire) {
        if (questionnaire!=null) return true;
        return donorRepository.donorHasQuestionnaire(id);
    }
}
