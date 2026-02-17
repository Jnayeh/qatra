package com.zayenha.qatra.donor.domain.model;

import jakarta.annotation.Nullable;

import java.time.LocalTime;

public record NotificationPreferences(
    NotificationFrequency frequency,
    @Nullable QuietHours quietHours,
    boolean allowEmergencyNotifications,
    int maxNotificationDistanceKm
) {
    public record QuietHours(
        LocalTime start,
        LocalTime end
    ) {}
}
