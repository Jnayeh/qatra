package com.zayenha.qatra.forecasting.infrastructure.persistence.repository;

import com.zayenha.qatra.forecasting.infrastructure.persistence.entity.DemandForecastEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DemandForecastJpaRepository extends JpaRepository<DemandForecastEntity, Long>, JpaSpecificationExecutor<DemandForecastEntity> {
}
