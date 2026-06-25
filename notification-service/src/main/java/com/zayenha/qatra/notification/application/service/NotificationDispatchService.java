package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.port.in.NotificationCommandUseCases;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NotificationDispatchService implements NotificationCommandUseCases {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationDeliveryService deliveryService;
    private final List<ChannelHandler> channels;

    public NotificationDispatchService(
            NotificationRepositoryPort notificationRepository,
            NotificationDeliveryService deliveryService,
            List<ChannelHandler> channels) {
        this.notificationRepository = notificationRepository;
        this.deliveryService = deliveryService;
        this.channels = channels;
    }

    @Transactional
    public void dispatch(NotificationPayload payload, String channelConfig) {
        if (notificationRepository.existsByCorrelationId(payload.correlationId())) {
            log.debug("Duplicate notification skipped: {}", payload.correlationId());
            return;
        }

        var configuredChannels = resolveChannels(channelConfig);
        Set<NotificationChannel> requestedChannels = payload.requestedChannels() != null
                ? payload.requestedChannels().stream().filter(configuredChannels::contains).collect(Collectors.toSet())
                : Set.of();

        var notification = notificationRepository.save(
                new Notification(payload.userId(), payload.email(), payload.emergencyId(), payload.appointmentId(),
                        payload.type(), payload.title(), payload.body(), payload.data(),
                        payload.correlationId(), List.copyOf(requestedChannels)));
        log.info("Notification saved: id={} type={} userId={} correlationId={}",
                notification.getId(), payload.type(), payload.userId(), payload.correlationId());

        for (var channel : channels) {
            if (!requestedChannels.contains(channel.type())) {
                continue;
            }
            log.info("Delivering via {} for userId={}", channel.type(), payload.userId());
            deliveryService.deliverWithRetry(channel, payload, notification);
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
}
