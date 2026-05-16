package com.zayenha.qatra.donor.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "donation_certificates")
@Getter
@Setter
@NoArgsConstructor
public class DonationCertificateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(name = "center_id", nullable = false)
    private Long centerId;

    @Column(name = "ml_collected")
    private Integer mlCollected;

    @Column(name = "donation_date", nullable = false)
    private LocalDate donationDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
