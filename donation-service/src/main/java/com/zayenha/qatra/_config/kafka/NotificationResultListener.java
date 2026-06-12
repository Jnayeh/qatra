package com.zayenha.qatra._config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zayenha.qatra._shared.event.NotificationResultEvent;
import com.zayenha.qatra.user.domain.model.verification.VerificationTokenType;
import com.zayenha.qatra.user.infrastructure.persistence.adapter.VerificationTokenRepositoryAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class NotificationResultListener {

    private final VerificationTokenRepositoryAdapter tokenRepo;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.notification-result.name:notification.result}")
    private String resultTopic;

    public NotificationResultListener(
        VerificationTokenRepositoryAdapter tokenRepo,
        ObjectMapper objectMapper) {
        this.tokenRepo = tokenRepo;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "#{'${kafka.topic.notification-result.name:notification.result}'}",
                   groupId = "donation-service")
    public void consumeResult(String message) {
        try {
            var result = objectMapper.readValue(message, NotificationResultEvent.class);
            log.info("Received result: userId={} status={} eventType={}",
                    result.userId(), result.status(), result.originalEventType());

            if ("DELIVERY_FAILED".equals(result.status())) {
                log.warn("Delivery FAILED for user {} — compensating: deleting verification token",
                        result.userId());
                compensate(result.userId(), result.originalEventType());
            } else {
                log.info("Delivery SUCCESS for user {} — saga complete", result.userId());
            }
        } catch (Exception e) {
            log.error("Failed to process notification result: {}", e.getMessage(), e);
        }
    }

    private void compensate(Long userId, String eventType) {
        VerificationTokenType type = "PasswordResetEvent".equals(eventType)
                ? VerificationTokenType.PASSWORD_RESET
                : VerificationTokenType.EMAIL_VERIFICATION;

        tokenRepo.findByUserIdAndType(userId, type).ifPresent(token -> {
            tokenRepo.deleteById(token.getId());
            log.info("[SAGA-COMPENSATION] Deleted {} token for user {}", type, userId);
        });
    }
}
