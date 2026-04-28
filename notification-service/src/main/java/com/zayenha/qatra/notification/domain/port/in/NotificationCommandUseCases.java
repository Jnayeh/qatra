package com.zayenha.qatra.notification.domain.port.in;

import com.zayenha.qatra.notification.domain.model.NotificationPayload;

public interface NotificationCommandUseCases {
    void dispatch(NotificationPayload payload, String channelConfig);
}