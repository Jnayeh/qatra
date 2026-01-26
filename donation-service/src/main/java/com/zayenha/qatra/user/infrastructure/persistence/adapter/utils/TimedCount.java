package com.zayenha.qatra.user.infrastructure.persistence.adapter.utils;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record TimedCount(Long count, @NotNull Instant ttl) {
}
