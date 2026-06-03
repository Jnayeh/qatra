package com.zayenha.qatra.notification.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistration;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import static org.mockito.Mockito.*;

class WebSocketConfigTest {

    @Test
    void shouldRegisterWebSocketHandler() {
        var handler = mock(NotificationBroadcastHandler.class);
        var interceptor = mock(JwtHandshakeInterceptor.class);
        var config = new WebSocketConfig(handler, interceptor);
        var registry = mock(WebSocketHandlerRegistry.class);
        var registration = mock(WebSocketHandlerRegistration.class);

        when(registry.addHandler(handler, "/ws/notifications")).thenReturn(registration);
        when(registration.addInterceptors(interceptor)).thenReturn(registration);

        config.registerWebSocketHandlers(registry);

        verify(registry).addHandler(handler, "/ws/notifications");
        verify(registration).addInterceptors(interceptor);
    }
}
