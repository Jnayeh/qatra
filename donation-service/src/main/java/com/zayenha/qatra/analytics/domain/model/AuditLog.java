package com.zayenha.qatra.analytics.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private String eventType;
    private Long actorId;
    private String actorEmail;
    private String targetType;
    private Long targetId;
    private String details;
    private String sourceModule;
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(String eventType, Long actorId, String targetType, Long targetId, String details, String sourceModule) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.sourceModule = sourceModule;
        this.timestamp = Instant.now();
    }
}
