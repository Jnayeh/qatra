package com.zayenha.qatra.notification.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationBroadcastHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationBroadcastHandler.class);

    private final Map<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public NotificationBroadcastHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.USER_ID_ATTR);
        if (userId == null) {
            try { session.close(); } catch (Exception ignored) {}
            return;
        }
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.USER_ID_ATTR);
        if (userId == null) return;
        var targets = userSessions.get(userId);
        if (targets == null) return;
        for (WebSocketSession s : targets) {
            if (s.isOpen() && !s.equals(session)) {
                try {
                    s.sendMessage(message);
                } catch (Exception e) {
                    log.error("Failed to relay message to session {}", s.getId(), e);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(JwtHandshakeInterceptor.USER_ID_ATTR);
        if (userId != null) {
            var sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable error) {
        afterConnectionClosed(session, null);
        try { session.close(); } catch (Exception ignored) {}
    }

    public void broadcast(Long userId, Object payload) {
        var sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) return;
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            log.error("Failed to serialize broadcast payload", e);
            return;
        }
        var message = new TextMessage(json);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (Exception e) {
                    log.error("Failed to broadcast to session {}", s.getId(), e);
                }
            }
        }
    }
}
