package com.zayenha.qatra.appointment.application.proxy;

import com.zayenha.qatra.donor.application.api.DonorApi;
import com.zayenha.qatra.donor.application.api.dto.DonorProfileDTO;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AptDonorProxy {

    private final DonorApi donorApi;

    public DonorProfileEntity getDonorReference(Long id) {
        return donorApi.getDonorReference(id);
    }

    public Optional<DonorProfileDTO> findDonorByUserId(Long userId) {
        return donorApi.findDonorByUserId(userId);
    }

    public DonorProfileDTO saveDonor(DonorProfileDTO dto) {
        return donorApi.saveDonor(dto);
    }
}
