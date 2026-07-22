package com.zayenha.qatra.donor.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.DonorStatus;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.donor.domain.port.in.DonorQueryUseCases;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.domain.service.DonorDomainValidator;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonationCertificateJpaRepository;
import com.zayenha.qatra.user.api.UserApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DonorService implements DonorCommandUseCases, DonorQueryUseCases {

    private final DonorRepositoryPort donorRepository;
    private final DonationCertificateJpaRepository certificateRepository;
    private final CacheService cacheService;
    private final AuditPublisher auditPublisher;
    private final UserApi userApi;

    private DonorDomainValidator validator() {
        return new DonorDomainValidator();
    }

    @Override
    @Transactional
    public DonorProfile updateProfile(Long userId) {
        var profile = donorRepository.findByUserId(userId).orElseGet(() -> {
            var newProfile = new DonorProfile(userId);
            return donorRepository.save(newProfile);
        });
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        cacheService.evictByPattern("impactResults:*");
        auditPublisher.publish(userId,"DONOR_PROFILE_UPDATED", saved.getId(), "DonorProfile", null, Map.of("userId", userId));
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateBloodType(Long userId, BloodType bloodType) {
        validator().validateBloodType(bloodType);
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        validator().validateBloodTypeUpdate(profile);
        profile.setBloodType(bloodType);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateBloodTypeAdmin(Long donorId, BloodType bloodType) {
        validator().validateBloodType(bloodType);
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        var oldBloodType = profile.getBloodType();
        profile.setBloodType(bloodType);
        profile.setBloodTypeVerified(true);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_BLOOD_TYPE_ADMIN_UPDATED", saved.getId(), "DonorProfile",
            Map.of("bloodType", oldBloodType),
            Map.of("bloodType", bloodType.name(), "verified", true));
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateLocation(Long userId, UpdateLocationCommand command) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        var oldLat = Optional.ofNullable(profile.getLatitude()).orElse(0D);
        var oldLon = Optional.ofNullable(profile.getLongitude()).orElse(0D);
        profile.setLatitude(command.latitude());
        profile.setLongitude(command.longitude());
        profile.setCity(command.city());
        profile.setUpdatedAt(Instant.now());
        boolean profileIncomplete = !Boolean.TRUE.equals(profile.getProfileComplete());
        if(profileIncomplete) {
            var hasQuestionnaire = donorRepository.donorHasQuestionnaire(profile.getId());
            profile.setProfileComplete(hasQuestionnaire);
        }
        var saved = donorRepository.save(profile);

        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_LOCATION_UPDATED", saved.getId(), "DonorProfile",
            Map.of("latitude", oldLat, "longitude", oldLon),
            Map.of("latitude", command.latitude(), "longitude", command.longitude(), "city", command.city()));
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateAvailability(Long userId, AvailabilityStatus status) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        var oldStatus = profile.getAvailability();
        profile.setAvailability(status);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_AVAILABILITY_UPDATED", saved.getId(), "DonorProfile",
            Map.of("availability", oldStatus),
            Map.of("availability", status.name()));
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateNotificationPrefs(Long userId, NotificationPreferences prefs) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(()-> new NotFoundException("Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setNotificationPreferences(prefs);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_NOTIFICATION_PREFS_UPDATED", saved.getId(), "DonorProfile", null,
            Map.of("userId", userId, "frequency",
                   prefs != null && prefs.frequency() != null ? prefs.frequency().name() : "null"));
        return saved;
    }

    @Override
    @Transactional
    public void requestDeletion(Long userId) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setStatus(DonorStatus.PENDING_DELETION);
        profile.setUpdatedAt(Instant.now());
        profile.setDeletionRequestedAt(Instant.now());
        donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        cacheService.evictByPattern("impactResults:*");
        auditPublisher.publish("DONOR_DELETION_REQUESTED", profile.getId(), "DonorProfile", null, Map.of("userId", userId));
    }

    @Override
    @Transactional
    public void reactivateDonor(Long userId) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        if (profile.getStatus() == DonorStatus.PENDING_DELETION) {
            var actor = AuditUtils.currentUserId()==0 ? userId : AuditUtils.currentUserId();
            profile.setStatus(DonorStatus.ACTIVE);
            profile.setUpdatedAt(Instant.now());
            donorRepository.save(profile);
            cacheService.evictByPattern("donorProfiles:*");
            auditPublisher.publish(actor, "DONOR_REACTIVATED", profile.getId(), "DonorProfile", null, Map.of("userId", userId, "previousStatus", "PENDING_DELETION"));
        }
    }

    @Override
    @Transactional
    public DonorProfile updateRestriction(Long donorId, boolean permanentlyRestricted, String reason) {
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        var oldRestricted = profile.getPermanentlyRestricted();
        profile.setPermanentlyRestricted(permanentlyRestricted);
        profile.setRestrictionReason(reason);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_RESTRICTION_UPDATED", saved.getId(), "DonorProfile",
            Map.of("restricted", oldRestricted),
            Map.of("restricted", permanentlyRestricted, "reason", reason));
        return saved;
    }

    @Override
    @Transactional
    public DonorProfile updateFlag(Long donorId, boolean flagged) {
        var profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        var oldFlagged = profile.getFlaggedForManualReview();
        profile.setFlaggedForManualReview(flagged);
        profile.setUpdatedAt(Instant.now());
        var saved = donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_FLAG_UPDATED", saved.getId(), "DonorProfile",
            Map.of("flagged", oldFlagged),
            Map.of("flagged", flagged));
        return saved;
    }

    @Override
    public void activateProfile(Long userId) {
        var profile = donorRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found by userID: " + userId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        profile.setStatus(DonorStatus.ACTIVE);
        profile.setUpdatedAt(Instant.now());
        donorRepository.save(profile);
        cacheService.evictByPattern("donorProfiles:*");
        auditPublisher.publish("DONOR_PROFILE_ACTIVATED", profile.getId(), "DonorProfile", null, Map.of("userId", userId));
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfile getMyProfile(Long userId) {
        var key = "donorProfiles:userId:" + userId;
        var cached = cacheService.get(key, DonorProfile.class);
        if (cached.isPresent()) return cached.get();
        var result = donorRepository.findByUserId(userId).orElseThrow(()-> new NotFoundException(
                "Donor not found by userID: " + userId,
                DonorErrorCode.DONOR_NOT_FOUND.name()));
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public DonorProfile getDonorById(Long donorId) {
        var key = "donorProfiles:id:" + donorId;
        var cached = cacheService.get(key, DonorProfile.class);
        if (cached.isPresent()) return cached.get();
        var result = donorRepository.findById(donorId)
                .orElseThrow(() -> new NotFoundException(
                        "Donor not found: " + donorId,
                        DonorErrorCode.DONOR_NOT_FOUND.name()));
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public ImpactResult getImpact(Long userId) {
        var key = "impactResults:" + userId;
        var cached = cacheService.get(key, ImpactResult.class);
        if (cached.isPresent()) return cached.get();
        var profile = donorRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException(
                "Donor not found by userID: " + userId, DonorErrorCode.DONOR_NOT_FOUND.name()));
        var milestones = new java.util.ArrayList<String>();
        if (profile.getTotalDonations() >= 1) milestones.add("First donation completed");
        if (profile.getTotalDonations() >= 5) milestones.add("Regular donator");
        if (profile.getTotalDonations() >= 10) milestones.add("Life saver");
        var result = new ImpactResult(profile.getTotalDonations(), milestones);
        cacheService.put(key, result);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateProjection> getCertificates(Long userId) {
        var profile = donorRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException(
                "Donor not found by userID: " + userId, DonorErrorCode.DONOR_NOT_FOUND.name()));
        return certificateRepository.findByDonorIdOrderByDonationDateDesc(profile.getId()).stream()
                .map(c -> new CertificateProjection(
                        c.getId(), c.getAppointmentId(), c.getDonorName(),
                        c.getCenterName(), c.getMlCollected(), c.getDonationDate()))
                .toList();
    }

}
