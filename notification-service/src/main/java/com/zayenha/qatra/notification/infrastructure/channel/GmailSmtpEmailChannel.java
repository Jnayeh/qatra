package com.zayenha.qatra.notification.infrastructure.channel;

import com.zayenha.qatra.notification.application.service.ChannelHandler;
import com.zayenha.qatra.notification.domain.exception.NotificationDeliveryException;
import com.zayenha.qatra.notification.domain.model.Notification;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "email.channel.provider", havingValue = "gmail")
public class GmailSmtpEmailChannel implements ChannelHandler {

    private static final Logger log = LoggerFactory.getLogger(GmailSmtpEmailChannel.class);

    private final JavaMailSender mailSender;
    private final String from;

    public GmailSmtpEmailChannel(JavaMailSender mailSender, @Value("${email.channel.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public NotificationChannel type() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void deliver(NotificationPayload payload, Notification notification) {
        var toEmail = payload.email();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Cannot send EMAIL for userId {}: no email address available", payload.userId());
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject(payload.title());
            helper.setText(payload.htmlBody(), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new NotificationDeliveryException("Gmail SMTP delivery failed", e);
        }
    }
}
