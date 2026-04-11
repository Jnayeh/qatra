package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannelType;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import com.zayenha.qatra.notification.domain.port.out.WebSocketPublisherPort;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationChannel implements NotificationChannel {

    private final NotificationRepositoryPort notificationRepository;
    private final WebSocketPublisherPort webSocketPublisher;

    public InAppNotificationChannel(NotificationRepositoryPort notificationRepository,
                                    WebSocketPublisherPort webSocketPublisher) {
        this.notificationRepository = notificationRepository;
        this.webSocketPublisher = webSocketPublisher;
    }

    @Override
    public NotificationChannelType type() {
        return NotificationChannelType.IN_APP;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        var notification = notificationRepository.save(
                new Notification(payload.userId(), payload.type(),
                        payload.title(), payload.body(), payload.data(),
                        payload.correlationId()));
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);
        webSocketPublisher.sendToUser(payload.userId(), notification);
    }
}
