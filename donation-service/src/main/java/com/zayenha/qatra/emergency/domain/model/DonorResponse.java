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
    private String reason;
    private Instant respondedAt;

    public DonorResponse() {}

    public DonorResponse(Long emergencyId, Long donorId) {
        this.emergencyId = emergencyId;
        this.donorId = donorId;
    }

    public void accept(Long slotId) {
        this.status = ResponseStatus.ACCEPTED;
        this.slotId = slotId;
        this.respondedAt = Instant.now();
    }

    public void decline(String reason) {
        this.status = ResponseStatus.DECLINED;
        this.reason = reason;
        this.respondedAt = Instant.now();
    }
}
