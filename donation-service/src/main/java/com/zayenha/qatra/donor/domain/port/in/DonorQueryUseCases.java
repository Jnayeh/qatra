package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.HealthQuestionnaire;

public interface DonorQueryUseCases {
    DonorProfile getMyProfile(Long userId);
    DonorProfile getDonorById(Long donorId);
    HealthQuestionnaire getHealthQuestionnaire(Long userId);
}
