package com.zayenha.qatra.report.application;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.center.application.api.CenterApi;
import com.zayenha.qatra.report.web.dto.CenterReportData;
import com.zayenha.qatra.report.adapter.CenterReportRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CenterReportService {

    private final CenterReportRepositoryPort repository;
    private final CenterApi centerApi;

    @Transactional(readOnly = true)
    public String generateCenterReport(Long centerId, LocalDate startDate, LocalDate endDate) {
        if (!centerApi.existsById(centerId)) {
            throw new NotFoundException("Center not found: " + centerId, "CENTER_NOT_FOUND");
        }
        return generateCsvReport(centerId, startDate, endDate);
    }

    public String generateCsvReport(Long centerId, LocalDate startDate, LocalDate endDate) {
        CenterReportData data = repository.getReportData(centerId, startDate, endDate);

        var sb = new StringBuilder();
        sb.append("Center Report,").append(centerId).append("\n");
        sb.append("Period,").append(startDate).append(" to ").append(endDate).append("\n\n");

        appendAppointmentSection(sb, data);
        sb.append("\n");
        appendEmergencySection(sb, data);
        sb.append("\n");
        appendDonorResponseSection(sb, data);

        return sb.toString();
    }

    private void appendAppointmentSection(StringBuilder sb, CenterReportData data) {
        sb.append("=== APPOINTMENTS ===\n");
        sb.append("Metric,Value\n");
        sb.append("Total Appointments,").append(data.totalAppointments()).append("\n");
        sb.append("Completed,").append(data.completedAppointments()).append("\n");
        sb.append("Cancelled,").append(data.cancelledAppointments()).append("\n");
        for (var entry : data.appointmentsByOutcome().entrySet()) {
            sb.append("Outcome ").append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
        sb.append("Total ml Collected,").append(data.totalMlCollected()).append("\n");
    }

    private void appendEmergencySection(StringBuilder sb, CenterReportData data) {
        sb.append("=== EMERGENCIES ===\n");
        sb.append("Metric,Value\n");
        sb.append("Total Emergencies,").append(data.totalEmergencies()).append("\n");
        for (var entry : data.emergenciesByStatus().entrySet()) {
            sb.append("Status ").append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
    }

    private void appendDonorResponseSection(StringBuilder sb, CenterReportData data) {
        sb.append("=== DONOR RESPONSES ===\n");
        sb.append("Metric,Value\n");
        sb.append("Total Responses,").append(data.totalDonorResponses()).append("\n");
        for (var entry : data.donorResponsesByStatus().entrySet()) {
            sb.append("Status ").append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
        }
    }
}
