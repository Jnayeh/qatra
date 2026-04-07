package com.zayenha.qatra.notification.domain.port.out;

public interface EmailSenderPort {

    void send(String to, String subject, String body);
}
