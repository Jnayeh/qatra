package com.zayenha.qatra.emergency.domain.port.in;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.emergency.domain.model.DonorResponse;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.model.MatchResultStats;
import com.zayenha.qatra.emergency.domain.model.ResponseStats;

import java.util.List;

public interface EmergencyQueryUseCases {
    EmergencyRequest findById(Long id);
    PageResult<EmergencyRequest> findAll(SearchCriteria criteria);
    PageResult<EmergencyRequest> findAll(SearchCriteria criteria, Long centerId, EmergencyStatus status);
    List<EmergencyRequest> findOpenByBloodType(BloodType bloodType);
    List<EmergencyRequest> findOpenWithinRadius(double latitude, double longitude, double radiusKm);
    List<DonorResponse> findResponsesByEmergencyId(Long emergencyId);
    List<DonorResponse> findResponsesByDonorId(Long donorId);
    MatchResultStats getMatchStats(Long emergencyId);
    ResponseStats getResponseStats(Long emergencyId);
}
