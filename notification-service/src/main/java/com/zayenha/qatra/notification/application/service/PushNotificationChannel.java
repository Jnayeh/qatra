package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(PushNotificationChannel.class);

    @Override
    public NotificationChannel type() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        log.info("Push notification for userId={}: title={}, body={}",
                payload.userId(), payload.title(), payload.body());
    }
}