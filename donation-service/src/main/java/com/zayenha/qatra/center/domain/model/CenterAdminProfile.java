package com.zayenha.qatra.center.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class CenterAdminProfile {
    private Long id;
    private Long userId;
    private Long centerId;
    private Instant createdAt;

    public CenterAdminProfile() {}

    public CenterAdminProfile(Long userId, Long centerId) {
        this.userId = userId;
        this.centerId = centerId;
        this.createdAt = Instant.now();
    }
}
