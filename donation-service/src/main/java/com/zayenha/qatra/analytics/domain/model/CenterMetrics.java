package com.zayenha.qatra.analytics.domain.model;

import java.util.List;
import java.util.Map;

public record CenterMetrics(
    long totalAppointments,
    long completedAppointments,
    long todayAppointments,
    long weekAppointments,
    long monthAppointments,
    long totalEmergencies,
    long fulfilledEmergencies,
    long todayEmergencies,
    long weekEmergencies,
    long monthEmergencies,
    long totalDonorResponses,
    long responseRate30d,
    long totalMlCollected,
    long activeEmergencies,
    List<Map<String, Object>> appointmentsByDay,
    List<Map<String, Object>> emergenciesByDay
) {}
