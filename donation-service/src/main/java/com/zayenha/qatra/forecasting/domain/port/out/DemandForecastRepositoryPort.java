package com.zayenha.qatra.forecasting.domain.port.out;

import com.zayenha.qatra.forecasting.domain.model.DemandForecast;

import java.util.List;

public interface DemandForecastRepositoryPort {
    List<DemandForecast> saveAll(List<DemandForecast> forecasts);
    List<DemandForecast> findFiltered(String bloodType, Long centerId, String city, String country, Integer year, Integer month);
    void deleteByYearAndMonth(int year, int month);
}
