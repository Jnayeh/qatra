package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationChannel implements ChannelHandler {

    private final NotificationRepositoryPort notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InAppNotificationChannel(NotificationRepositoryPort notificationRepository,
                                    SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public com.zayenha.qatra.notification.domain.model.NotificationChannel type() {
        return com.zayenha.qatra.notification.domain.model.NotificationChannel.IN_APP;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        var notification = notificationRepository.save(
                new Notification(payload.userId(), payload.email(), payload.emergencyId(), payload.appointmentId(),
                        payload.type(), payload.title(), payload.body(), payload.data(),
                        payload.correlationId(), payload.channel()));
        notification.setStatus(NotificationStatus.SENT);
        notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(
                payload.userId().toString(), "/queue/notifications", notification);
    }
}
