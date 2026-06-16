package com.zayenha.qatra.report.adapter;

import com.zayenha.qatra.appointment.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.DonorResponseJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.report.web.dto.CenterReportData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CenterReportRepositoryAdapter implements CenterReportRepositoryPort {

    private final AppointmentJpaRepository appointmentRepo;
    private final EmergencyJpaRepository emergencyRepo;
    private final DonorResponseJpaRepository donorResponseRepo;

    @Override
    public CenterReportData getReportData(Long centerId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        long totalAppointments = appointmentRepo.countByCenterIdAndCreatedAtBetween(centerId, null, start, end);
        long completedAppointments = appointmentRepo.countByCenterIdAndCreatedAtBetween(centerId, "COMPLETED", start, end);
        long cancelledAppointments = appointmentRepo.countByCenterIdAndCreatedAtBetween(centerId, "CANCELLED", start, end);
        Map<String, Long> appointmentsByOutcome = toGroupedMap(appointmentRepo.countByCenterIdGroupedByOutcome(centerId, start, end));
        long totalMlCollected = appointmentRepo.sumMlCollectedByCenterIdAndCreatedAtBetween(centerId, start, end);

        long totalEmergencies = emergencyRepo.countByCenterIdAndCreatedAtBetween(centerId, start, end);
        Map<String, Long> emergenciesByStatus = toGroupedMap(
            emergencyRepo.countByCenterIdGroupedByStatus(centerId, start, end));

        long totalDonorResponses = donorResponseRepo.countByCenterIdAndCreatedAtBetween(centerId, start, end);
        Map<String, Long> donorResponsesByStatus = toGroupedMap(
            donorResponseRepo.countByCenterIdGroupedByStatus(centerId, start, end));

        return new CenterReportData(
            totalAppointments,
            completedAppointments,
            cancelledAppointments,
            appointmentsByOutcome,
            totalMlCollected,
            totalEmergencies,
            emergenciesByStatus,
            totalDonorResponses,
            donorResponsesByStatus
        );
    }

    private Map<String, Long> toGroupedMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (var row : rows) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }
}
