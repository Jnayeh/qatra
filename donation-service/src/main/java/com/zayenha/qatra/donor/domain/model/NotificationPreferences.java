package com.zayenha.qatra.donor.domain.model;

import java.time.LocalTime;

public record NotificationPreferences(
    NotificationFrequency frequency,
    QuietHours quietHours,
    boolean allowEmergencyNotifications,
    int maxNotificationDistanceKm
) {
    public record QuietHours(
        LocalTime start,
        LocalTime end
    ) {}
}
