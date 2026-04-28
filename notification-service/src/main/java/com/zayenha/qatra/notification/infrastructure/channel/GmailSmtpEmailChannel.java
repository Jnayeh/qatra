package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "gmail")
public class GmailSmtpEmailChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(GmailSmtpEmailChannel.class);

    private final JavaMailSender mailSender;

    public GmailSmtpEmailChannel(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel type() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void deliver(NotificationPayload payload) {
        var toEmail = payload.email();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send EMAIL for userId {}: no email address available", payload.userId());
            return;
        }
        try {
            var message = new SimpleMailMessage();
            message.setFrom("noreply@qatra.com");
            message.setTo(toEmail);
            message.setSubject(payload.title());
            message.setText(payload.body());
            mailSender.send(message);
        } catch (Exception e) {
            throw new NotificationDeliveryException("Gmail SMTP delivery failed", e);
        }
    }
}
