package com.zayenha.qatra._shared.event;

import java.time.Instant;

public record GDPRDeletionRequestedEvent(
    Long userId,
    String reason,
    Instant occurredAt
) {}
