package com.zayenha.qatra.emergency.infrastructure.persistence.entity;

import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "donor_response_seq")
    @SequenceGenerator(name = "donor_response_seq", sequenceName = "donor_response_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id", nullable = false)
    private EmergencyRequestEntity emergency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfileEntity donor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id")
    private SlotEntity slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus status;

    private String reason;

    private Instant respondedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
