package com.zayenha.qatra.emergency.domain.port.out;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.MatchResult;

import java.util.List;
import java.util.Optional;

public interface EmergencyRepositoryPort {
    EmergencyRequest save(EmergencyRequest request);
    Optional<EmergencyRequest> findById(Long id);
    PageResult<EmergencyRequest> findAll(SearchCriteria criteria);
    List<EmergencyRequest> findByBloodTypeAndStatus(BloodType bloodType, EmergencyStatus status);
    List<EmergencyRequest> findByStatus(EmergencyStatus status);
    DonorResponse saveResponse(DonorResponse response);
    Optional<DonorResponse> findResponseById(Long id);
    List<DonorResponse> findResponsesByEmergencyId(Long emergencyId);
    List<DonorResponse> findResponsesByDonorId(Long donorId);
    boolean existsByEmergencyIdAndDonorId(Long emergencyId, Long donorId);
    MatchResult saveMatchResult(MatchResult matchResult);
    Optional<MatchResult> findMatchResultByEmergencyIdAndDonorId(Long emergencyId, Long donorId);
    List<MatchResult> findMatchResultsByEmergencyId(Long emergencyId);
    List<MatchResult> findMatchResultsByDonorId(Long donorId);
}
