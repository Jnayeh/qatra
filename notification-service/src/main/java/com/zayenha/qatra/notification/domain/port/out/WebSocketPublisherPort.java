package com.zayenha.qatra.notification.domain.port.out;

public interface WebSocketPublisherPort {

    void sendToUser(Long userId, Object payload);
}
