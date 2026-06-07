package com.zayenha.qatra.emergency.infrastructure.web.dto.request;

import jakarta.validation.constraints.Size;

public record ResolveEmergencyRequest(
    @Size(max = 500) String notes
) {}
