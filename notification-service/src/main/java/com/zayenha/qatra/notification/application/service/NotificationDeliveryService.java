package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class NotificationDeliveryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDeliveryService.class);

    private final NotificationRepositoryPort notificationRepository;

    public NotificationDeliveryService(NotificationRepositoryPort notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Retryable(
        retryFor = com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException.class,
        maxAttemptsExpression = "${notification.retry.max-attempts:3}",
        backoff = @Backoff(delayExpression = "${notification.retry.backoff-base-ms:2000}",
                           multiplierExpression = "2"))
    public void deliverWithRetry(ChannelHandler channel, NotificationPayload payload,
                                   Notification notification) {
        channel.deliver(payload, notification);
        notification.markSent();
        notificationRepository.save(notification);
        log.info("Channel {} delivery confirmed for notification id={}", channel.type(), notification.getId());
    }
}
