package com.zayenha.qatra.emergency.domain.port.in;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;

import java.util.List;
import java.util.Optional;

public interface EmergencyQueryUseCases {
    Optional<EmergencyRequest> findById(Long id);
    PageResult<EmergencyRequest> findAll(SearchCriteria criteria);
    List<EmergencyRequest> findOpenByBloodType(BloodType bloodType);
    List<EmergencyRequest> findOpenWithinRadius(double latitude, double longitude, double radiusKm);
    List<DonorResponse> findResponsesByEmergencyId(Long emergencyId);
    List<DonorResponse> findResponsesByDonorId(Long donorId);
}
