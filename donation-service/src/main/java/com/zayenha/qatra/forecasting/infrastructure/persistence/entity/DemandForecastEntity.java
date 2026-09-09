package com.zayenha.qatra.forecasting.infrastructure.persistence.entity;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.forecasting.domain.model.ForecastPeriodType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "demand_forecasts")
@Getter
@Setter
@NoArgsConstructor
public class DemandForecastEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "demand_forecast_seq")
    @SequenceGenerator(name = "demand_forecast_seq", sequenceName = "demand_forecast_seq", initialValue = 500)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BloodType bloodType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id")
    private CenterEntity center;

    private String city;

    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForecastPeriodType forecastPeriodType;

    private Integer weekNumber;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Double predictedDemand;

    private Double confidence;

    @Column(nullable = false, updatable = false)
    private Instant generatedAt;

    @PrePersist
    void onCreate() {
        generatedAt = Instant.now();
    }
}
