package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.port.out.NotificationRepositoryPort;
import com.zayenha.qatra.notification.infrastructure.config.NotificationBroadcastHandler;
import org.springframework.stereotype.Component;

@Component
public class InAppNotificationChannel implements ChannelHandler {

    private final NotificationRepositoryPort notificationRepository;
    private final NotificationBroadcastHandler broadcastHandler;

    public InAppNotificationChannel(NotificationRepositoryPort notificationRepository,
                                    NotificationBroadcastHandler broadcastHandler) {
        this.notificationRepository = notificationRepository;
        this.broadcastHandler = broadcastHandler;
    }

    @Override
    public NotificationChannel type() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public void deliver(NotificationPayload payload, Notification notification) {
        broadcastHandler.broadcast(notification.getUserId(), notification);
    }
}
