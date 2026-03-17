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
    private String processedBy;

    public GDPRDeletionRequest() {}

    public GDPRDeletionRequest(Long userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.status = GDPRDeletionStatus.PENDING;
        this.requestedAt = Instant.now();
    }

    public void approve(String processedBy) {
        this.status = GDPRDeletionStatus.APPROVED;
        this.processedAt = Instant.now();
        this.processedBy = processedBy;
    }

    public void reject(String processedBy) {
        this.status = GDPRDeletionStatus.REJECTED;
        this.processedAt = Instant.now();
        this.processedBy = processedBy;
    }

    public void complete() {
        this.status = GDPRDeletionStatus.COMPLETED;
    }
}
