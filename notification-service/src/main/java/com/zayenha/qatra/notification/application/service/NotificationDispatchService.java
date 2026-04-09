package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannelType;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationDispatchService {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationRepositoryPort notificationRepository;
    private final List<NotificationChannel> channels;
    private final int maxRetryAttempts;
    private final long backoffBaseMs;

    public NotificationDispatchService(
            NotificationRepositoryPort notificationRepository,
            List<NotificationChannel> channels,
            @Value("${notification.retry.max-attempts:3}") int maxRetryAttempts,
            @Value("${notification.retry.backoff-base-ms:2000}") long backoffBaseMs) {
        this.notificationRepository = notificationRepository;
        this.channels = channels;
        this.maxRetryAttempts = maxRetryAttempts;
        this.backoffBaseMs = backoffBaseMs;
    }

    public void dispatch(NotificationPayload payload, String channelConfig) {
        if (notificationRepository.existsByCorrelationId(payload.correlationId())) {
            log.debug("Duplicate notification skipped: {}", payload.correlationId());
            return;
        }

        var channelsToUse = resolveChannels(channelConfig);
        var notification = notificationRepository.save(
                new Notification(payload.userId(), payload.type(), payload.title(),
                        payload.body(), payload.data(), payload.correlationId()));

        for (var channel : channels) {
            if (!channelsToUse.contains(channel.type())) {
                continue;
            }
            deliverWithRetry(channel, payload, notification, 1);
        }
    }

    private Set<NotificationChannelType> resolveChannels(String channelConfig) {
        return Arrays.stream(channelConfig.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(c -> !c.isEmpty())
                .map(NotificationChannelType::valueOf)
                .collect(Collectors.toSet());
    }

    private void deliverWithRetry(NotificationChannel channel, NotificationPayload payload,
                                   Notification notification, int attempt) {
        try {
            channel.deliver(payload);
            notification.markSent();
            notificationRepository.save(notification);
        } catch (NotificationDeliveryException e) {
            log.warn("Delivery attempt {} failed for channel {}: {}",
                    attempt, channel.type(), e.getMessage());
            if (attempt < maxRetryAttempts) {
                sleep(backoffBaseMs * (1L << (attempt - 1)));
                deliverWithRetry(channel, payload, notification, attempt + 1);
            } else {
                log.error("All {} retry attempts exhausted for channel {} on correlationId {}",
                        maxRetryAttempts, channel.type(), payload.correlationId());
                notification.markFailed();
                notificationRepository.save(notification);
                // ponytail: publish to DLQ topic would go here
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
