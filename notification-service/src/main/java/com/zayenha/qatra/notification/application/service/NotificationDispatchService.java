package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.port.in.NotificationCommandUseCases;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationDispatchService implements NotificationCommandUseCases {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationRepositoryPort notificationRepository;
    private final List<ChannelHandler> channels;
    private final int maxRetryAttempts;
    private final long backoffBaseMs;

    public NotificationDispatchService(
            NotificationRepositoryPort notificationRepository,
            List<ChannelHandler> channels,
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
                new Notification(payload.userId(), payload.email(), payload.emergencyId(), payload.appointmentId(),
                        payload.type(), payload.title(), payload.body(), payload.data(),
                        payload.correlationId(), payload.channel()));

        for (var channel : channels) {
            if (!channelsToUse.contains(channel.type())) {
                continue;
            }
            deliverWithRetry(channel, payload, notification);
        }
    }

    private Set<NotificationChannel> resolveChannels(String channelConfig) {
        return Arrays.stream(channelConfig.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(c -> !c.isEmpty())
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());
    }

    @Retryable(
        retryFor = NotificationDeliveryException.class,
        maxAttemptsExpression = "${notification.retry.max-attempts:3}",
        backoff = @Backoff(delayExpression = "${notification.retry.backoff-base-ms:2000}",
                           multiplierExpression = "2"))
    private void deliverWithRetry(ChannelHandler channel, NotificationPayload payload,
                                   Notification notification) {
        channel.deliver(payload);
        notification.markSent();
        notificationRepository.save(notification);
    }
}
