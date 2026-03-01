package com.zayenha.qatra.center.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateClosureRequest(
    @NotNull LocalDate date,
    String startTime,
    String endTime,
    boolean allDay,
    @NotBlank String reason
) {}
