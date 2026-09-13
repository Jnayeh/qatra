package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.user.infrastructure.web.dto.response.RestrictedUserResponse;

import java.util.*;

public interface DonorQueryUseCases {
    DonorProfile getMyProfile(Long userId);
    DonorProfile getDonorById(Long donorId);
    Optional<DonorProfile> findByUserId(Long userId);
    ImpactResult getImpact(Long userId);

    record ImpactResult(int totalDonations, List<String> milestones,
                        Map<String, Integer> donationsByType,
                        Map<String, Integer> monthlyDonations) {}

    List<CertificateProjection> getCertificates(Long userId);

    record CertificateProjection(Long id, Long appointmentId, String donorName, String centerName, Integer mlCollected, java.time.LocalDate donationDate) {}

    PageResult<RestrictedUserResponse> getPermanentlyRestrictedDonors(int page, int size);
}
