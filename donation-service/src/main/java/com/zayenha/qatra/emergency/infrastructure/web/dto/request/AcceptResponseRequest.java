package com.zayenha.qatra.emergency.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;

public record AcceptResponseRequest(
    @NotNull Long slotId
) {}
