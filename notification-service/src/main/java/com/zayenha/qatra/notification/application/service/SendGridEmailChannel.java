package com.zayenha.qatra.notification.application.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "sendgrid")
public class SendGridEmailChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(SendGridEmailChannel.class);

    private final SendGrid sendGrid;

    public SendGridEmailChannel(@Value("${sendgrid.api-key}") String apiKey) {
        this.sendGrid = new SendGrid(apiKey);
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
            var from = new Email("noreply@qatra.com");
            var to = new Email(toEmail);
            var content = new Content("text/plain", payload.body());
            var mail = new Mail(from, payload.title(), to, content);

            var request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            var response = sendGrid.api(request);
            log.debug("SendGrid email sent to {}: status={}", toEmail, response.getStatusCode());
        } catch (Exception e) {
            throw new NotificationDeliveryException("SendGrid delivery failed", e);
        }
    }

    private static String resolveEmail(NotificationPayload payload) {
        if (payload.email() != null && !payload.email().isBlank()) return payload.email();
        return null;
    }
}