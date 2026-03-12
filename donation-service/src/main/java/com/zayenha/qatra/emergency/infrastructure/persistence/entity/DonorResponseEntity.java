package com.zayenha.qatra.emergency.infrastructure.persistence.entity;

import com.zayenha.qatra.emergency.domain.model.ResponseStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "donor_responses")
@Getter
@Setter
@NoArgsConstructor
public class DonorResponseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long emergencyId;

    @Column(nullable = false)
    private Long donorId;

    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus status;

    private Instant respondedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
