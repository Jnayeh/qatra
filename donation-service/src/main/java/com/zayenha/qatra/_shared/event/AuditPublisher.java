package com.zayenha.qatra._shared.event;

import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(String action, Long entityId, String entityType,
                        Map<String, Object> oldValue, Map<String, Object> newValue) {
        publish(AuditUtils.currentUserId(), action, entityId, entityType, oldValue, newValue);
    }

    public void publish(Long actorId, String action, Long entityId, String entityType,
                        Map<String, Object> oldValue, Map<String, Object> newValue) {
        try {
            eventPublisher.publishEvent(new AuditEvent(
                    actorId, action, entityType, entityId,
                    oldValue, newValue, null, null));
        } catch (PersistenceException e) {
            log.error("Failed to publish audit event due to exception: {}", e.getMessage());
        }
    }
}