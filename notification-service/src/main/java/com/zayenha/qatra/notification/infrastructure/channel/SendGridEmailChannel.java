package com.zayenha.qatra.notification.infrastructure.channel;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.zayenha.qatra.notification.application.service.NotificationChannel;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannelType;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "sendgrid", matchIfMissing = true)
public class SendGridEmailChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailChannel.class);

    private final SendGrid sendGrid;

    public SendGridEmailChannel(@Value("${sendgrid.api-key:}") String apiKey) {
        if (apiKey != null && !apiKey.isEmpty()) {
            this.sendGrid = new SendGrid(apiKey);
        } else {
            this.sendGrid = null;
        }
    }

    @Override
    public NotificationChannelType type() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        if (sendGrid == null) {
            log.warn("SendGrid API key not configured, skipping email for user {}", payload.userId());
            return;
        }
        try {
            var from = new Email("noreply@qatra.com");
            var to = new Email(payload.userId().toString()); // ponytail: resolve email from user service
            var content = new Content("text/plain", payload.body());
            var mail = new Mail(from, payload.title(), to, content);

            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sendGrid.api(request);
        } catch (Exception e) {
            throw new NotificationDeliveryException("SendGrid delivery failed", e);
        }
    }
}
