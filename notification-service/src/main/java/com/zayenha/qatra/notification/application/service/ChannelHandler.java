package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;

public interface ChannelHandler {

    NotificationChannel type();

    void deliver(NotificationPayload payload) throws NotificationDeliveryException;
}
