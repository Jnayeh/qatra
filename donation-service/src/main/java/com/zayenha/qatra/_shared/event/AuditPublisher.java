package com.zayenha.qatra._shared.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(String action, Long entityId, String entityType,
                        Map<String, Object> oldValue, Map<String, Object> newValue) {
        eventPublisher.publishEvent(new AuditEvent(
            AuditUtils.currentUserId(), action, entityType, entityId,
            oldValue, newValue, null, null));
    }
}