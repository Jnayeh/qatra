package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "resend")
public class ResendEmailChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailChannel.class);

    private final RestTemplate restTemplate;
    private final String apiKey;

    public ResendEmailChannel(
            
            @Value("${resend.api-key}") String apiKey) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
    }

    @Override
    public NotificationChannel type() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        var toEmail = resolveEmail(payload);
        if (toEmail == null) {
            log.warn("Cannot send EMAIL for userId {}: no email address available", payload.userId());
            return;
        }
        try {
            var headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            var body = Map.of(
                "from", "noreply@qatra.com",
                "to", toEmail,
                "subject", payload.title(),
                "text", payload.body()
            );

            var request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity("https://api.resend.com/emails", request, String.class);
            log.debug("Resend email sent to {}", toEmail);
        } catch (Exception e) {
            throw new NotificationDeliveryException("Resend delivery failed", e);
        }
    }

    private static String resolveEmail(NotificationPayload payload) {
        if (payload.email() != null && !payload.email().isBlank()) return payload.email();
        return null;
    }
}
