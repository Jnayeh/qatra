package com.zayenha.qatra.system.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class GDPRDeletionRequest {
    private Long id;
    private Long userId;
    private String reason;
    private GDPRDeletionStatus status;
    private Instant requestedAt;
    private Instant processedAt;

    public GDPRDeletionRequest() {}

    public GDPRDeletionRequest(Long userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.status = GDPRDeletionStatus.IN_PROGRESS;
        this.requestedAt = Instant.now();
    }

    public void complete() {
        this.status = GDPRDeletionStatus.COMPLETED;
        this.processedAt = Instant.now();
    }

    public void cancel() {
        this.status = GDPRDeletionStatus.CANCELED;
        this.processedAt = Instant.now();
    }
}
