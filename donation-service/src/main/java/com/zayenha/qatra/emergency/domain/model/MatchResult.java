package com.zayenha.qatra.emergency.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MatchResult {
    private Long id;
    private Long emergencyId;
    private Long centerId;
    private Long donorId;
    private Long radius;
    private BloodType bloodType;
    private Integer escalationLevel;
    private MatchStatus status;
    private Instant respondedAt;
    private Instant createdAt;

    public MatchResult() {}

    public MatchResult(Long emergencyId, Long centerId, Long donorId,
                       Long radius, BloodType bloodType, Integer escalationLevel) {
        this.emergencyId = emergencyId;
        this.centerId = centerId;
        this.donorId = donorId;
        this.radius = radius;
        this.bloodType = bloodType;
        this.escalationLevel = escalationLevel;
        this.status = MatchStatus.PENDING;
        this.createdAt = Instant.now();
    }
}
