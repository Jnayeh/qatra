package com.zayenha.qatra.forecasting.domain.model;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandForecast {
    private Long id;
    private BloodType bloodType;
    private Long centerId;
    private String city;
    private String country;
    private ForecastPeriodType forecastPeriodType;
    private Integer weekNumber;
    private int year;
    private int month;
    private double predictedDemand;
    private Double confidence;
    private Instant generatedAt;
}
