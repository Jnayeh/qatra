package com.zayenha.qatra.donor.application;

import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.domain.service.DonorDomainValidator;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

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
        var hasQuestionnaire = donorRepository.donorHasQuestionnaire(profile.getId());
        profile.setProfileComplete(command.latitude() != null && command.longitude() != null && hasQuestionnaire);
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
    public void requestDeletion(Long userId) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
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
    @Transactional(readOnly = true)
    public ImpactResult getImpact(Long userId) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException(
                "Donor not found: " + userId, DonorErrorCode.DONOR_NOT_FOUND.name()));
        var milestones = new java.util.ArrayList<String>();
        if (profile.getTotalDonations() >= 1) milestones.add("First donation completed");
        if (profile.getTotalDonations() >= 5) milestones.add("5 donations milestone");
        if (profile.getTotalDonations() >= 10) milestones.add("10 donations milestone");
        if (profile.getEstimatedLivesSaved() >= 10) milestones.add("Saved 10+ lives");
        return new ImpactResult(profile.getTotalDonations(), profile.getEstimatedLivesSaved(), milestones);
    }

}
