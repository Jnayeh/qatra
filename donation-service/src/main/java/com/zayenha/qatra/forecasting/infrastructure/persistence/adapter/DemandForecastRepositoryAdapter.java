package com.zayenha.qatra.forecasting.infrastructure.persistence.adapter;

import com.zayenha.qatra.forecasting.domain.model.DemandForecast;
import com.zayenha.qatra.forecasting.domain.port.out.DemandForecastRepositoryPort;
import com.zayenha.qatra.forecasting.infrastructure.mapper.ForecastMapper;
import com.zayenha.qatra.forecasting.infrastructure.persistence.entity.DemandForecastEntity;
import com.zayenha.qatra.forecasting.infrastructure.persistence.repository.DemandForecastJpaRepository;
import com.zayenha.qatra.forecasting.infrastructure.persistence.repository.DemandForecastSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DemandForecastRepositoryAdapter implements DemandForecastRepositoryPort {

    private final DemandForecastJpaRepository jpaRepository;
    private final ForecastMapper mapper;

    @Override
    public List<DemandForecast> saveAll(List<DemandForecast> forecasts) {
        var entities = forecasts.stream().map(mapper::toEntity).toList();
        var saved = jpaRepository.saveAll(entities);
        return saved.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<DemandForecast> findFiltered(String bloodType, Long centerId, String city, String country, Integer year, Integer month) {
        var spec = DemandForecastSpec.build(bloodType, centerId, city, country, year, month);
        return jpaRepository.findAll(spec).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByYearAndMonth(int year, int month) {
        var spec = DemandForecastSpec.build(null, null, null, null, year, month);
        var toDelete = jpaRepository.findAll(spec);
        jpaRepository.deleteAll(toDelete);
    }
}
