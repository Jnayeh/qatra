package com.zayenha.qatra.analytics.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue, String ipAddress, String userAgent) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.timestamp = Instant.now();
    }
}
