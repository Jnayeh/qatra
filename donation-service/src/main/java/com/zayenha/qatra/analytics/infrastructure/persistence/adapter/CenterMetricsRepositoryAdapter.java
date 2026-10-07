package com.zayenha.qatra.analytics.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.LogAction;
import com.zayenha.qatra.analytics.domain.model.CenterMetrics;
import com.zayenha.qatra.analytics.domain.port.out.CenterMetricsRepositoryPort;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogJpaRepository;
import com.zayenha.qatra.analytics.infrastructure.persistence.repository.AuditLogSpec;
import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.zayenha.qatra._shared.domain.DateUtils;

import java.time.*;
import java.util.*;

@Component
@RequiredArgsConstructor
public class CenterMetricsRepositoryAdapter implements CenterMetricsRepositoryPort {

    private static final int DAYS_IN_SERIES = 30;

    private final AuditLogJpaRepository auditLogRepo;
    private final AppointmentJpaRepository appointmentRepo;
    private final EmergencyJpaRepository emergencyRepo;

    @Override
    public CenterMetrics getMetrics(Long centerId) {
        var now = Instant.now();
        var today = DateUtils.utcToday();
        var todayStart = DateUtils.toStartOfDay(today);
        var tomorrowStart = DateUtils.toStartOfDay(today.plusDays(1));
        var weekStart = DateUtils.toStartOfDay(today.with(DayOfWeek.MONDAY));
        var monthStart = DateUtils.toStartOfDay(today.withDayOfMonth(1));
        var thirtyDaysAgo = now.minus(Duration.ofDays(30));
        var firstDay = today.minusDays(DAYS_IN_SERIES - 1L);

        // Appointment metrics come from the appointments module (the `appointments` table),
        // not the audit log. Audit rows are filtered by center staff users via AuditLogSpec,
        // but bookings are made by donors, so those counts were always 0 and the dashboard
        // charts showed "No reservations yet" despite existing appointments.
        long totalAppointments = appointmentRepo.countByCenterId(centerId);
        long completedAppointments = appointmentRepo.countByCenterIdAndStatus(centerId, AppointmentStatus.COMPLETED.name());
        long todayAppointments = appointmentRepo.countByCenterIdInPeriod(centerId, todayStart, tomorrowStart);
        long weekAppointments = appointmentRepo.countByCenterIdInPeriod(centerId, weekStart, tomorrowStart);
        long monthAppointments = appointmentRepo.countByCenterIdInPeriod(centerId, monthStart, tomorrowStart);

        // Emergency response metrics still come from the audit log (emergency workflow events).
        // todayEmergencies is sourced from the appointments module instead: it counts today's
        // appointments booked as EMERGENCY, so the dashboard pie chart
        // (regular = todayAppointments - todayEmergencies) is an exact partition of today's bookings.
        long totalEmergencies = auditLogRepo.count(AuditLogSpec.build(LogAction.EMERGENCY_CREATED, null, null, centerId));
        long fulfilledEmergencies = auditLogRepo.count(AuditLogSpec.build(LogAction.EMERGENCY_FULFILLED, null, null, centerId));
        long todayEmergencies = appointmentRepo.countByCenterIdAndTypeInPeriod(centerId, AppointmentType.EMERGENCY.name(), todayStart, tomorrowStart);
        long weekEmergencies = auditLogRepo.count(AuditLogSpec.build(LogAction.EMERGENCY_CREATED, weekStart, now, centerId));
        long monthEmergencies = auditLogRepo.count(AuditLogSpec.build(LogAction.EMERGENCY_CREATED, monthStart, now, centerId));

        long totalAccepted = auditLogRepo.count(AuditLogSpec.build(LogAction.RESPONSE_ACCEPTED, null, null, centerId));
        long totalDeclined = auditLogRepo.count(AuditLogSpec.build(LogAction.RESPONSE_DECLINED, null, null, centerId));
        long totalDonorResponses = totalAccepted + totalDeclined;
        long accepted30d = auditLogRepo.count(AuditLogSpec.build(LogAction.RESPONSE_ACCEPTED, thirtyDaysAgo, now, centerId));
        long declined30d = auditLogRepo.count(AuditLogSpec.build(LogAction.RESPONSE_DECLINED, thirtyDaysAgo, now, centerId));

        long totalMlCollected = appointmentRepo.sumMlCollectedByCenterId(centerId);
        long activeEmergencies = emergencyRepo.countActiveByCenterId(centerId);

        // Zero-filled so the bar chart always renders a complete 30-day axis even on quiet days.
        var appointmentsByDay = daySeries(firstDay,
            appointmentRepo.countByCenterIdGroupedByDay(centerId, DateUtils.toStartOfDay(firstDay), tomorrowStart));
        var emergenciesByDay = mapByDay(
            auditLogRepo.countByCenterAndActionByDay(LogAction.EMERGENCY_CREATED.name(), centerId, thirtyDaysAgo, now));

        return new CenterMetrics(
            totalAppointments, completedAppointments,
            todayAppointments, weekAppointments, monthAppointments,
            totalEmergencies, fulfilledEmergencies,
            todayEmergencies, weekEmergencies, monthEmergencies,
            totalAccepted, totalDeclined, totalDonorResponses,
            accepted30d, declined30d,
            totalMlCollected, activeEmergencies,
            appointmentsByDay, emergenciesByDay
        );
    }

    /**
     * Builds a contiguous series from {@code firstDay} to today, padding missing days with 0.
     * Each entry uses the {@code "date"} key expected by the Angular {@code DayCount} contract.
     */
    private List<Map<String, Object>> daySeries(LocalDate firstDay, List<Object[]> rows) {
        var counts = new HashMap<String, Long>();
        for (var row : rows) {
            counts.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        var result = new ArrayList<Map<String, Object>>();
        var today = DateUtils.utcToday();
        for (var day = firstDay; !day.isAfter(today); day = day.plusDays(1)) {
            var key = day.toString();
            result.add(Map.of("date", key, "count", counts.getOrDefault(key, 0L)));
        }
        return result;
    }

    private List<Map<String, Object>> mapByDay(List<Object[]> rows) {
        var result = new ArrayList<Map<String, Object>>();
        for (var row : rows) {
            // Key must be "date" (not "day"): every Angular consumer reads DayCount.date.
            result.add(Map.of("date", row[0].toString(), "count", ((Number) row[1]).longValue()));
        }
        return result;
    }
}
