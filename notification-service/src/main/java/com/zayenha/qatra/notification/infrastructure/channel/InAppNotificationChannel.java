package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
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
    public NotificationChannel type() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void deliver(NotificationPayload payload, Notification notification) {
        messagingTemplate.convertAndSendToUser(
                payload.userId().toString(), "/queue/notifications", notification);
    }
}
