package com.zayenha.qatra.notification.infrastructure.config;

import com.zayenha.qatra.notification.infrastructure.security.JwtTokenProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    public static final String USER_ID_ATTR = "userId";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtHandshakeInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        var queryParams = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        var token = queryParams.getFirst("token");
        if (token == null || token.isBlank()) {
            log.debug("WebSocket handshake rejected: missing token");
            return false;
        }
        if (!jwtTokenProvider.validateToken(token)) {
            log.debug("WebSocket handshake rejected: invalid token");
            return false;
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        attributes.put(USER_ID_ATTR, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}
