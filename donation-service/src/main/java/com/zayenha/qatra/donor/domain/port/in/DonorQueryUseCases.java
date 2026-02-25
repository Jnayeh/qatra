package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.DonorProfile;

public interface DonorQueryUseCases {
    DonorProfile getMyProfile(Long userId);
    DonorProfile getDonorById(Long donorId);
}
