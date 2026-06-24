package com.zayenha.qatra.emergency.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.emergency.domain.model.MatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "match_results")
@Getter
@Setter
@NoArgsConstructor
public class MatchResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "match_result_seq")
    @SequenceGenerator(name = "match_result_seq", sequenceName = "match_result_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_id", nullable = false)
    private EmergencyRequestEntity emergency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private CenterEntity center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id", nullable = false)
    private DonorProfileEntity donor;

    @Column(nullable = false)
    private Long radius;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @Column(nullable = false)
    private Integer escalationLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    private Instant respondedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
