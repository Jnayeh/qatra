package com.zayenha.qatra.notification.domain.model;

import java.time.Instant;

public class Notification {

    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String body;
    private String data;
    private String correlationId;
    private NotificationStatus status;
    private Instant createdAt;
    private Instant readAt;

    public Notification() {}

    public Notification(Long userId, String type, String title, String body,
                        String data, String correlationId) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.data = data;
        this.correlationId = correlationId;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setTitle(String title) { this.title = title; }
    public String getTitle() { return title; }
    public void setBody(String body) { this.body = body; }
    public String getBody() { return body; }
    public void setData(String data) { this.data = data; }
    public String getData() { return data; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public void markSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }
}
