package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra.analytics.domain.model.CenterMetrics;
import com.zayenha.qatra.analytics.domain.port.out.CenterMetricsRepositoryPort;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogJpaRepository;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogSpec;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CenterMetricsRepositoryAdapter implements CenterMetricsRepositoryPort {

    private final AuditLogJpaRepository auditLogRepo;
    private final AppointmentJpaRepository appointmentRepo;
    private final EmergencyJpaRepository emergencyRepo;

    @Override
    public CenterMetrics getMetrics(Long centerId) {
        var now = Instant.now();
        var today = LocalDate.now();
        var zone = ZoneId.systemDefault();
        var todayStart = today.atStartOfDay(zone).toInstant();
        var weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay(zone).toInstant();
        var monthStart = today.withDayOfMonth(1).atStartOfDay(zone).toInstant();
        var thirtyDaysAgo = now.minus(Duration.ofDays(30));

        long totalAppointments = auditLogRepo.count(AuditLogSpec.build("APPOINTMENT_CREATED", null, null, centerId));
        long completedAppointments = auditLogRepo.count(AuditLogSpec.build("APPOINTMENT_COMPLETED", null, null, centerId));
        long todayAppointments = auditLogRepo.count(AuditLogSpec.build("APPOINTMENT_CREATED", todayStart, now, centerId));
        long weekAppointments = auditLogRepo.count(AuditLogSpec.build("APPOINTMENT_CREATED", weekStart, now, centerId));
        long monthAppointments = auditLogRepo.count(AuditLogSpec.build("APPOINTMENT_CREATED", monthStart, now, centerId));

        long totalEmergencies = auditLogRepo.count(AuditLogSpec.build("EMERGENCY_CREATED", null, null, centerId));
        long fulfilledEmergencies = auditLogRepo.count(AuditLogSpec.build("EMERGENCY_FULFILLED", null, null, centerId));
        long todayEmergencies = auditLogRepo.count(AuditLogSpec.build("EMERGENCY_CREATED", todayStart, now, centerId));
        long weekEmergencies = auditLogRepo.count(AuditLogSpec.build("EMERGENCY_CREATED", weekStart, now, centerId));
        long monthEmergencies = auditLogRepo.count(AuditLogSpec.build("EMERGENCY_CREATED", monthStart, now, centerId));

        long totalDonorResponses = auditLogRepo.count(AuditLogSpec.build("DONOR_RESPONSE", null, null, centerId));
        long responseRate30d = auditLogRepo.count(AuditLogSpec.build("DONOR_RESPONSE", thirtyDaysAgo, now, centerId));

        long totalMlCollected = appointmentRepo.sumMlCollectedByCenterId(centerId);
        long activeEmergencies = emergencyRepo.countActiveByCenterId(centerId);

        var appointmentsByDay = mapByDay(
            auditLogRepo.countByCenterAndActionByDay("APPOINTMENT_CREATED", centerId, thirtyDaysAgo, now));
        var emergenciesByDay = mapByDay(
            auditLogRepo.countByCenterAndActionByDay("EMERGENCY_CREATED", centerId, thirtyDaysAgo, now));

        return new CenterMetrics(
            totalAppointments, completedAppointments,
            todayAppointments, weekAppointments, monthAppointments,
            totalEmergencies, fulfilledEmergencies,
            todayEmergencies, weekEmergencies, monthEmergencies,
            totalDonorResponses, responseRate30d,
            totalMlCollected, activeEmergencies,
            appointmentsByDay, emergenciesByDay
        );
    }

    private List<Map<String, Object>> mapByDay(List<Object[]> rows) {
        var result = new ArrayList<Map<String, Object>>();
        for (var row : rows) {
            result.add(Map.of("day", row[0].toString(), "count", ((Number) row[1]).longValue()));
        }
        return result;
    }
}
