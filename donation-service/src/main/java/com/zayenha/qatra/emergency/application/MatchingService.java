package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.domain.GeoUtils;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.donor.application.api.dto.DonorProfileDTO;
import com.zayenha.qatra.emergency.application.proxy.EmergencyCenterProxy;
import com.zayenha.qatra.emergency.application.proxy.EmergencyDonorProxy;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.MatchResult;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final EmergencyDonorProxy donorProxy;
    private final EmergencyRepositoryPort emergencyRepository;
    private final EmergencyCenterProxy centerProxy;
    private final EventPublisherPort eventPublisherPort;

    @Value("${emergency.escalation-radius-increment:10}")
    private int radiusIncrementKm;

    @Transactional
    public void matchDonors(EmergencyRequest emergency) {
        var center = centerProxy.findCenterById(emergency.getCenterId()).orElse(null);
        if (center == null || center.getLatitude() == null || center.getLongitude() == null) {
            log.warn("Cannot match donors for emergency {}: center has no location", emergency.getId());
            return;
        }

        var candidates = donorProxy.findEligibleForEmergency().stream()
                .filter(d -> d.getBloodType().canDonateTo(emergency.getBloodType()))
                .filter(d -> !d.getUserId().equals(emergency.getCreatedByStaffId()))
                .toList();

        var alreadyMatchedIds = emergencyRepository.findMatchResultsByEmergencyId(emergency.getId())
                .stream().map(MatchResult::getDonorId).toList();

        var available = candidates.stream()
                .filter(d -> !alreadyMatchedIds.contains(d.getUserId()))
                .toList();

        if (available.isEmpty()) {
            log.info("No eligible donors found for emergency {}", emergency.getId());
            return;
        }

        var centerLat = center.getLatitude();
        var centerLon = center.getLongitude();
        var matched = new ArrayList<MatchedDonor>();
        var radius = emergency.getMatchRadius() != null ? emergency.getMatchRadius() : 0;

        while (true) {
            matched.clear();
            for (var donor : available) {
                var dist = GeoUtils.distanceKm(centerLat, centerLon,
                        donor.getLatitude(), donor.getLongitude());
                if (dist <= radius) {
                    matched.add(new MatchedDonor(donor, dist));
                }
            }

            if (matched.size() >= emergency.getUnitsNeeded()) {
                break;
            }

            var nextRadius = radius + radiusIncrementKm;
            if (nextRadius > 200) {
                log.info("Emergency {}: radius expanded to {} km with {} donors, still insufficient",
                        emergency.getId(), radius, matched.size());
                break;
            }
            radius = nextRadius;
        }

        matched.sort(Comparator.comparingDouble(MatchedDonor::distance)
                .thenComparing(Comparator.comparingDouble(
                        md -> md.donor().getReliabilityScore() != null
                                ? -md.donor().getReliabilityScore() : 0.0)));

        var selected = matched.stream()
                .limit(emergency.getUnitsNeeded())
                .toList();

        for (var md : selected) {
            var matchResult = new MatchResult(
                    emergency.getId(), emergency.getCenterId(),
                    md.donor().getUserId(),
                    (long) radius,
                    md.donor().getBloodType(),
                    emergency.getEscalationLevel()
            );
            emergencyRepository.saveMatchResult(matchResult);
            eventPublisherPort.publishNotificationDispatch(
                    md.donor().getUserId(), null, "EMERGENCY_ALERT",
                    "Urgent: Blood Donation Needed",
                    "An emergency blood request has been created at a nearby center. Please respond.",
                    Map.of("emergencyId", emergency.getId())
            );
        }

        if (radius > emergency.getMatchRadius()) {
            emergency.setMatchRadius(radius);
            emergencyRepository.save(emergency);
        }

        log.info("Emergency {} matched with {} donors at radius {} km",
                emergency.getId(), selected.size(), radius);
    }

    private record MatchedDonor(DonorProfileDTO donor, double distance) {}
}
