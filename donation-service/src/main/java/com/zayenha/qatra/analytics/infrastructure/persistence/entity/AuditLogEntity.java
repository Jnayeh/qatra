package com.zayenha.qatra.analytics.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(nullable = false)
    private String action;

    private String entityType;
    private Long entityId;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    private String ipAddress;
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @PrePersist
    void onCreate() {
        if (timestamp == null) timestamp = Instant.now();
    }
}
