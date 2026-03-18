package com.zayenha.qatra.system.infrastructure.persistence.entity;

import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "gdpr_deletion_requests")
@Getter
@Setter
@NoArgsConstructor
public class GDPRDeletionRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GDPRDeletionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant processedAt;
    private String processedBy;

    @PrePersist
    void onCreate() {
        requestedAt = Instant.now();
    }
}
