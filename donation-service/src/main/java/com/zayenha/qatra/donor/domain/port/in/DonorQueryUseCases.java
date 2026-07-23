package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.user.infrastructure.web.dto.response.RestrictedUserResponse;

public interface DonorQueryUseCases {
    DonorProfile getMyProfile(Long userId);
    DonorProfile getDonorById(Long donorId);
    ImpactResult getImpact(Long userId);

    record ImpactResult(int totalDonations, java.util.List<String> milestones) {}

    java.util.List<CertificateProjection> getCertificates(Long userId);

    record CertificateProjection(Long id, Long appointmentId, String donorName, String centerName, Integer mlCollected, java.time.LocalDate donationDate) {}

    PageResult<RestrictedUserResponse> getPermanentlyRestrictedDonors(int page, int size);
}
