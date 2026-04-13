package com.zayenha.qatra.notification.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    @Test
    void shouldRegisterStompEndpoint() {
        var config = new WebSocketConfig();
        var registry = mock(StompEndpointRegistry.class);
        var registration = mock(StompWebSocketEndpointRegistration.class);

        when(registry.addEndpoint("/ws/notifications")).thenReturn(registration);
        when(registration.setAllowedOriginPatterns(any(String.class))).thenReturn(registration);
        when(registration.withSockJS()).thenReturn(null);

        config.registerStompEndpoints(registry);

        verify(registry).addEndpoint("/ws/notifications");
        verify(registration).setAllowedOriginPatterns("*");
        verify(registration).withSockJS();
    }
}
