package com.zayenha.qatra.report.web.dto;

import java.util.Map;

public record CenterReportData(
    long totalAppointments,
    long completedAppointments,
    long cancelledAppointments,
    Map<String, Long> appointmentsByOutcome,
    long totalMlCollected,
    long totalEmergencies,
    Map<String, Long> emergenciesByStatus,
    long totalDonorResponses,
    Map<String, Long> donorResponsesByStatus
) {}
