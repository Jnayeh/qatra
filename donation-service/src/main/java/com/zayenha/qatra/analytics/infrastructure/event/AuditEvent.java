package com.zayenha.qatra.analytics.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {

    private final String eventType;
    private final Long actorId;
    private final String targetType;
    private final Long targetId;
    private final String details;
    private final String sourceModule;

    public AuditEvent(Object source, String eventType, Long actorId, String targetType, Long targetId, String details, String sourceModule) {
        super(source);
        this.eventType = eventType;
        this.actorId = actorId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.sourceModule = sourceModule;
    }

}
