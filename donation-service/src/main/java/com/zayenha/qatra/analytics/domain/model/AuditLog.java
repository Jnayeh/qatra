package com.zayenha.qatra.analytics.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private Map<String, Object> oldValue;
    private Map<String, Object> newValue;
    private String ipAddress;
    private Instant timestamp;

    public AuditLog() {}

    public AuditLog(Long userId, String action, String entityType, Long entityId,
                    Map<String, Object> oldValue, Map<String, Object> newValue, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.timestamp = Instant.now();
    }
}
