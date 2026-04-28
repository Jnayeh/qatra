package com.zayenha.qatra.emergency.application;

import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.emergency.domain.model.EmergencyStatus;
import com.zayenha.qatra.emergency.domain.port.out.EmergencyRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyMonitoringService {

    private final EmergencyRepositoryPort emergencyRepository;
    private final AppointmentRepositoryPort appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MatchingService matchingService;

    @Value("${escalation-after-minutes:30}")
    private int escalateAfterMinutes;

    @Value("${emergency.escalation-radius-increment:10}")
    private int escalationRadiusIncrement;

    private static final int ML_PER_UNIT = 450;

    @Scheduled(fixedRateString = "${emergency.monitoring-interval-ms:300000}")
    @Transactional
    public void monitorEmergencies() {
        log.debug("Running emergency monitoring cycle");
        var open = emergencyRepository.findByStatus(EmergencyStatus.OPEN);
        var now = Instant.now();

        for (var emergency : open) {
            if (emergency.getExpiresAt() == null) continue;

            if (now.isAfter(emergency.getExpiresAt())) {
                expire(emergency);
            } else if (shouldEscalate(emergency, now)) {
                escalateIfNeeded(emergency);
            }
        }
    }

    private boolean shouldEscalate(EmergencyRequest emergency, Instant now) {
        var threshold = Optional.ofNullable(emergency.getUpdatedAt()).orElse(emergency.getCreatedAt())
                .plus(escalateAfterMinutes, ChronoUnit.MINUTES);
        return now.isAfter(threshold);
    }

    private void expire(EmergencyRequest emergency) {
        emergency.updateStatus(EmergencyStatus.EXPIRED);
        emergencyRepository.save(emergency);
        log.info("Emergency {} expired", emergency.getId());
        eventPublisher.publishEvent(new AuditEvent(
            AuditUtils.currentUserId(), "EMERGENCY_EXPIRED", "EmergencyRequest",
            emergency.getId(),
            Map.of("status", EmergencyStatus.OPEN.name()),
            Map.of("status", EmergencyStatus.EXPIRED.name()),
            null, null));
    }

    private void escalateIfNeeded(EmergencyRequest emergency) {
        var unitsCollected = appointmentRepository.countCompletedByEmergencyId(emergency.getId());
        if (unitsCollected >= emergency.getUnitsNeeded()) {
            emergency.fulfill();
            emergencyRepository.save(emergency);
            log.info("Emergency {} auto-fulfilled ({} units collected)", emergency.getId(), unitsCollected);
            eventPublisher.publishEvent(new AuditEvent(
                AuditUtils.currentUserId(), "EMERGENCY_FULFILLED", "EmergencyRequest",
                emergency.getId(),
                Map.of("status", EmergencyStatus.OPEN.name()),
                Map.of("status", EmergencyStatus.FULFILLED.name(), "unitsCollected", unitsCollected),
                null, null));
            return;
        }

        var newLevel = emergency.getEscalationLevel() != null
                ? emergency.getEscalationLevel() + 1 : 1;
        var newRadius = (emergency.getMatchRadius() != null
                ? emergency.getMatchRadius() : 0) + escalationRadiusIncrement;
        emergency.setEscalationLevel(newLevel);
        emergency.setMatchRadius(newRadius);
        emergencyRepository.save(emergency);
        log.info("Emergency {} escalated to level {} with radius {} km",
                emergency.getId(), newLevel, newRadius);
        eventPublisher.publishEvent(new AuditEvent(
            AuditUtils.currentUserId(), "EMERGENCY_ESCALATED", "EmergencyRequest",
            emergency.getId(),
            null,
            Map.of("escalationLevel", newLevel, "matchRadius", newRadius),
            null, null));
        matchingService.matchDonors(emergency);
    }
}
