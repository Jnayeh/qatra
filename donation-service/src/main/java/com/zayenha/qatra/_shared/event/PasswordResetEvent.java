package com.zayenha.qatra._shared.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PasswordResetEvent(
    Long userId,
    String email,
    String resetToken,
    String resetLink,
    String correlationId,
    Instant occurredAt,
    List<String> channels
) {}
