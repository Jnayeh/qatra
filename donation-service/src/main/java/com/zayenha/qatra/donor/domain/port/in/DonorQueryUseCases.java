package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.DonorProfile;

public interface DonorQueryUseCases {
    DonorProfile getMyProfile(Long userId);
    DonorProfile getDonorById(Long donorId);
    ImpactResult getImpact(Long userId);

    record ImpactResult(int totalDonations, int estimatedLivesSaved, java.util.List<String> milestones) {}

    java.util.List<CertificateProjection> getCertificates(Long userId);

    record CertificateProjection(Long id, Long appointmentId, String donorName, String centerName, Integer mlCollected, java.time.LocalDate donationDate) {}
}
