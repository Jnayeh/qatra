package com.zayenha.qatra.emergency.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DonorResponse {
    private Long id;
    private Long emergencyId;
    private Long donorId;
    private Long slotId;
    private ResponseStatus status;
    private Instant respondedAt;
    private Instant createdAt;

    public DonorResponse() {}

    public DonorResponse(Long emergencyId, Long donorId) {
        this.emergencyId = emergencyId;
        this.donorId = donorId;
        this.status = ResponseStatus.PENDING;
    }

    public void accept(Long slotId) {
        this.status = ResponseStatus.ACCEPTED;
        this.slotId = slotId;
        this.respondedAt = Instant.now();
    }

    public void decline() {
        this.status = ResponseStatus.DECLINED;
        this.respondedAt = Instant.now();
    }
}
