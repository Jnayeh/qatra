package com.zayenha.qatra.notification.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zayenha.qatra.notification.application.dto.NotificationResultEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationResultPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationResultPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.notification-result.name:notification.result}")
    private String resultTopic;

    public NotificationResultPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishResult(String correlationId, String eventType, Long userId,
                              String status, String errorMessage) {
        try {
            var event = new NotificationResultEvent(correlationId, eventType, userId,
                    status, errorMessage, Instant.now());
            var payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(resultTopic, "result-" + userId, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[SAGA] Failed to publish result for userId={}: {}",
                                    userId, ex.getMessage(), ex);
                        } else {
                            log.info("[SAGA] Published result: userId={} status={}", userId, status);
                        }
                    });
        } catch (Exception e) {
            log.error("[SAGA] Failed to serialize result for userId={}: {}", userId, e.getMessage(), e);
        }
    }
}
