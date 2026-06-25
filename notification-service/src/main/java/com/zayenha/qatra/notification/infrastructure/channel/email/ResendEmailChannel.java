package com.zayenha.qatra.notification.infrastructure.channel.email;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.infrastructure.adapter.persistence.JpaNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "resend")
public class ResendEmailChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailChannel.class);
    private final JpaNotificationRepository jpaRepository;
    private final Resend resend;
    private final String from;

    public ResendEmailChannel(JpaNotificationRepository jpaRepository,
            @Value("${resend.api-key}") String apiKey,
            @Value("${email.channel.from}") String from) {
        this.jpaRepository = jpaRepository;
        this.resend = new Resend(apiKey);
        this.from = from;
    }

    @Override
    public NotificationChannel type() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void deliver(NotificationPayload payload, Notification notification) {
        var toEmail = resolveEmail(payload);
        if (toEmail == null) {
            log.warn("Cannot send EMAIL for userId {}: no email address available", payload.userId());
            return;
        }
        try {
            var params = CreateEmailOptions.builder()
                    .from(from)
                    .to(toEmail)
                    .subject(payload.title())
                    .html(payload.htmlBody())
                    .build();

            var response = resend.emails().send(params);
            notification.setStatus(NotificationStatus.DELIVERED);
            log.info("Resend email sent to {} (id={})", toEmail, response.getId());
        } catch (Exception e) {
            log.error("Resend email failed for email {}, exception: {}", toEmail, e.getMessage(), e);
            notification.setStatus(NotificationStatus.FAILED);
            throw new NotificationDeliveryException("Resend delivery failed", e);
        }
    }

    private static String resolveEmail(NotificationPayload payload) {
        if (payload.email() != null && !payload.email().isBlank()) return payload.email();
        return null;
    }
}
