package com.zayenha.qatra.notification.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Notification {
    private Long id;
    private Long userId;
    private String email;
    private Long emergencyId;
    private Long appointmentId;
    private NotificationType type;
    private String title;
    private String body;
    private Map<String, Object> data;
    private String correlationId;
    private List<NotificationChannel> channels;
    private NotificationStatus status;
    private Instant createdAt;
    private Instant sentAt;
    private Instant readAt;

    public Notification(Long userId, String email, Long emergencyId, Long appointmentId,
                        NotificationType type, String title, String body,
                        Map<String, Object> data, String correlationId, List<NotificationChannel> channels) {
        this.userId = userId;
        this.email = email;
        this.emergencyId = emergencyId;
        this.appointmentId = appointmentId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.data = data;
        this.correlationId = correlationId;
        this.channels = channels != null ? channels : List.of();
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }
}
