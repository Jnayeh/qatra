package com.zayenha.qatra.center.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CenterStaffProfile {
    private Long id;
    private Long userId;
    private Long centerId;
    private boolean verified;
    private Instant createdAt;

    public CenterStaffProfile() {}

    public CenterStaffProfile(Long userId, Long centerId) {
        this.userId = userId;
        this.centerId = centerId;
        this.verified = true;
        this.createdAt = Instant.now();
    }
}
