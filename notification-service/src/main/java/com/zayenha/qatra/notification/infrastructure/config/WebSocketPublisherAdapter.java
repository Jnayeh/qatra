package com.zayenha.qatra.notification.infrastructure.config;

import com.zayenha.qatra.notification.domain.port.out.WebSocketPublisherPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketPublisherAdapter implements WebSocketPublisherPort {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketPublisherAdapter(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendToUser(Long userId, Object payload) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                payload);
    }
}
