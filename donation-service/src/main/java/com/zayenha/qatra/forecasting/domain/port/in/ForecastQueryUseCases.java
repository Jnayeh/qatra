package com.zayenha.qatra.forecasting.domain.port.in;

import com.zayenha.qatra.forecasting.domain.model.DemandForecast;

import java.util.List;

public interface ForecastQueryUseCases {
    List<DemandForecast> getForecasts(String bloodType, Long centerId, String city, String country, Integer year, Integer month);
}
