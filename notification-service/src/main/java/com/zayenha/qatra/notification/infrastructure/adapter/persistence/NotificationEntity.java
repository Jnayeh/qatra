package com.zayenha.qatra.notification.infrastructure.adapter.persistence;

import com.zayenha.qatra.notification.domain.model.NotificationStatus;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String email;

    private Long emergencyId;

    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> data;

    private String correlationId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String channels;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant sentAt;

    private Instant readAt;
}
