package com.zayenha.qatra.emergency.application.proxy;

import com.zayenha.qatra.donor.application.api.DonorApi;
import com.zayenha.qatra.donor.application.api.dto.DonorProfileDTO;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmergencyDonorProxy {

    private final DonorApi donorApi;

    public DonorProfileEntity getDonorReference(Long id) {
        return donorApi.getDonorReference(id);
    }

    public Optional<DonorProfileDTO> findByUserId(Long userId) {
        return donorApi.findDonorByUserId(userId);
    }

    public List<DonorProfileDTO> findEligibleForEmergency() {
        return donorApi.findEligibleForEmergency();
    }

    public DonorProfileDTO saveDonor(DonorProfileDTO dto) {
        return donorApi.saveDonor(dto);
    }
}
