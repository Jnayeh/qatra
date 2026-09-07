package com.zayenha.qatra.forecasting.infrastructure.web.dto.response;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.forecasting.domain.model.ForecastPeriodType;

import java.time.Instant;

public record DemandForecastResponse(
        Long id,
        BloodType bloodType,
        Long centerId,
        String city,
        String country,
        ForecastPeriodType forecastPeriodType,
        Integer weekNumber,
        int year,
        int month,
        double predictedDemand,
        Double confidence,
        Instant generatedAt
) {
}
