package com.zayenha.qatra.donor.infrastructure.web.dto.request;

import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import jakarta.validation.constraints.NotNull;

public record UpdateNotificationPrefsRequest(
    @NotNull NotificationPreferences preferences
) {}
