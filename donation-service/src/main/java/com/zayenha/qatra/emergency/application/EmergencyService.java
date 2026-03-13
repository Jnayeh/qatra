package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.emergency.domain.exception.EmergencyErrorCode;
import com.zayenha.qatra.emergency.domain.model.*;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyCommandUseCases;
import com.zayenha.qatra.emergency.domain.port.in.EmergencyQueryUseCases;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmergencyService implements EmergencyCommandUseCases, EmergencyQueryUseCases {

    private final EmergencyRepositoryPort repository;

    @Override
    @Transactional
    public EmergencyRequest create(String patientName, BloodType bloodType, Integer unitsNeeded,
                                    EmergencyUrgency urgency, String hospital, Double latitude, Double longitude,
                                    String contactPhone) {
        var request = new EmergencyRequest(patientName, bloodType, unitsNeeded, urgency, hospital, latitude, longitude, contactPhone);
        request.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));
        return repository.save(request);
    }

    @Override
    @Transactional
    public EmergencyRequest update(Long id, String patientName, BloodType bloodType, Integer unitsNeeded,
                                    EmergencyUrgency urgency, String hospital, Double latitude, Double longitude,
                                    String contactPhone) {
        var request = findOrThrow(id);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Only open emergencies can be updated",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        request.setPatientName(patientName);
        request.setBloodType(bloodType);
        request.setUnitsNeeded(unitsNeeded);
        request.setUrgency(urgency);
        request.setHospital(hospital);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setContactPhone(contactPhone);
        return repository.save(request);
    }

    @Override
    @Transactional
    public EmergencyRequest cancel(Long id) {
        var request = findOrThrow(id);
        if (request.getStatus() == EmergencyStatus.FULFILLED) {
            throw new ValidationException("Cannot cancel a fulfilled emergency",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        request.cancel();
        return repository.save(request);
    }

    @Override
    @Transactional
    public DonorResponse respond(Long emergencyId, Long donorId) {
        var request = findOrThrow(emergencyId);
        if (request.getStatus() != EmergencyStatus.OPEN) {
            throw new ValidationException("Emergency is no longer open",
                    EmergencyErrorCode.EMERGENCY_ALREADY_FULFILLED.name());
        }
        if (repository.existsByEmergencyIdAndDonorId(emergencyId, donorId)) {
            throw new ConflictException("Donor already responded to this emergency",
                    EmergencyErrorCode.RESPONSE_ALREADY_EXISTS.name());
        }
        var response = new DonorResponse(emergencyId, donorId);
        return repository.saveResponse(response);
    }

    @Override
    @Transactional
    public DonorResponse acceptResponse(Long responseId, Long slotId) {
        var response = findResponseOrThrow(responseId);
        if (response.getStatus() != ResponseStatus.PENDING) {
            throw new ValidationException("Response is not in pending status",
                    EmergencyErrorCode.INVALID_RESPONSE_STATUS.name());
        }
        response.accept(slotId);
        var saved = repository.saveResponse(response);
        var emergency = findOrThrow(response.getEmergencyId());
        emergency.fulfill();
        repository.save(emergency);
        return saved;
    }

    @Override
    @Transactional
    public DonorResponse declineResponse(Long responseId) {
        var response = findResponseOrThrow(responseId);
        if (response.getStatus() != ResponseStatus.PENDING) {
            throw new ValidationException("Response is not in pending status",
                    EmergencyErrorCode.INVALID_RESPONSE_STATUS.name());
        }
        response.decline();
        return repository.saveResponse(response);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmergencyRequest> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<EmergencyRequest> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenByBloodType(BloodType bloodType) {
        return repository.findByBloodTypeAndStatus(bloodType, EmergencyStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyRequest> findOpenWithinRadius(double latitude, double longitude, double radiusKm) {
        return repository.findAll(SearchCriteria.defaultAll()).content().stream()
                .filter(e -> e.getStatus() == EmergencyStatus.OPEN)
                .filter(e -> {
                    if (e.getLatitude() == null || e.getLongitude() == null) return false;
                    double dist = haversine(latitude, longitude, e.getLatitude(), e.getLongitude());
                    return dist <= radiusKm;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> findResponsesByEmergencyId(Long emergencyId) {
        return repository.findResponsesByEmergencyId(emergencyId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonorResponse> findResponsesByDonorId(Long donorId) {
        return repository.findResponsesByDonorId(donorId);
    }

    private EmergencyRequest findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Emergency not found: " + id,
                        EmergencyErrorCode.EMERGENCY_NOT_FOUND.name()));
    }

    private DonorResponse findResponseOrThrow(Long id) {
        return repository.findResponseById(id)
                .orElseThrow(() -> new NotFoundException("Response not found: " + id,
                        EmergencyErrorCode.RESPONSE_NOT_FOUND.name()));
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
