package com.zayenha.qatra.system.infrastructure.persistence.entity;

import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GDPRDeletionStatus status;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant processedAt;

    @PrePersist
    void onCreate() {
        requestedAt = Instant.now();
    }
}
