package com.zayenha.qatra.analytics.infrastructure.event;

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
            event.getEventType(),
            event.getActorId(),
            event.getTargetType(),
            event.getTargetId(),
            event.getDetails(),
            event.getSourceModule()
        );
    }
}
