package com.zayenha.qatra.analytics.infrastructure.event;

import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra.analytics.application.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogService auditLogService;

    @EventListener
    public void onAuditEvent(AuditEvent event) {
        auditLogService.record(
            event.userId(),
            event.action(),
            event.entityType(),
            event.entityId(),
            event.oldValue(),
            event.newValue(),
            event.ipAddress(),
            event.userAgent()
        );
    }
}
