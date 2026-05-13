package com.zayenha.qatra.donor.application.api.dto;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileDTO {
    private Long id;
    private Long userId;
    private BloodType bloodType;
    private Double latitude;
    private Double longitude;
    private Double reliabilityScore;
    private int totalDonations;
    private LocalDate lastDonationDate;
    private LocalDate eligibleFromDate;
    private Integer consecutiveEmergencyDeclines;
    private Boolean flaggedForManualReview;
    private Instant updatedAt;
    private Instant lastAcceptAt;
    private Instant deletedAt;
}
