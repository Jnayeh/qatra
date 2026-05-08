package com.zayenha.qatra.donor.application.api;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.donor.application.api.dto.DonorProfileDTO;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonorJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DonorApi implements EntityApi<DonorProfileEntity> {

    private final DonorJpaRepository donorJpaRepository;
    private final DonorRepositoryPort donorRepositoryPort;

    public DonorProfileEntity getDonorReference(Long id) {
        return donorJpaRepository.getReferenceById(id);
    }

    @Override
    public DonorProfileEntity getReference(Long id) {
        return getDonorReference(id);
    }

    public Optional<DonorProfileDTO> findDonorByUserId(Long userId) {
        return donorRepositoryPort.findByUserId(userId).map(DonorApi::toDTO);
    }

    public Optional<DonorProfileDTO> findByDonorId(Long donorId) {
        return donorRepositoryPort.findById(donorId).map(DonorApi::toDTO);
    }

    public List<DonorProfileDTO> findEligibleForEmergency() {
        return donorRepositoryPort.findEligibleForEmergency().stream()
            .map(DonorApi::toDTO).toList();
    }

    public DonorProfileDTO saveDonor(DonorProfileDTO dto) {
        var profile = donorRepositoryPort.findByUserId(dto.getUserId())
            .orElseThrow(() -> new NotFoundException("Donor profile not found: " + dto.getUserId(), "DONOR_NOT_FOUND"));
        profile.setTotalDonations(dto.getTotalDonations());
        profile.setLastDonationDate(dto.getLastDonationDate());
        profile.setEligibleFromDate(dto.getEligibleFromDate());
        profile.setReliabilityScore(dto.getReliabilityScore());
        profile.setConsecutiveEmergencyDeclines(dto.getConsecutiveEmergencyDeclines());
        profile.setFlaggedForManualReview(dto.getFlaggedForManualReview());
        profile.setUpdatedAt(dto.getUpdatedAt());
        if (dto.getConsecutiveEmergencyDeclines() != null && dto.getConsecutiveEmergencyDeclines() == 0) {
            profile.resetConsecutiveDeclinesOnAccept();
        }
        var saved = donorRepositoryPort.save(profile);
        return toDTO(saved);
    }

    private static DonorProfileDTO toDTO(DonorProfile p) {
        return new DonorProfileDTO(
            p.getId(), p.getUserId(), p.getBloodType(),
            p.getLatitude(), p.getLongitude(), p.getReliabilityScore(),
            p.getTotalDonations(), p.getLastDonationDate(), p.getEligibleFromDate(),
            p.getConsecutiveEmergencyDeclines(), p.getFlaggedForManualReview(), p.getUpdatedAt()
        );
    }
}
