package com.zayenha.qatra.notification.application.service;

import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannelType;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;

public interface NotificationChannel {

    NotificationChannelType type();

    void deliver(NotificationPayload payload) throws NotificationDeliveryException;
}
