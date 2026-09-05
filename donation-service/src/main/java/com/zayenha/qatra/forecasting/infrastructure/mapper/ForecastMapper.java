package com.zayenha.qatra.forecasting.infrastructure.mapper;

import com.zayenha.qatra.forecasting.application.proxy.ForecastCenterProxy;
import com.zayenha.qatra.forecasting.domain.model.DemandForecast;
import com.zayenha.qatra.forecasting.infrastructure.persistence.entity.DemandForecastEntity;
import com.zayenha.qatra.forecasting.infrastructure.web.dto.response.DemandForecastResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class ForecastMapper {

    @Autowired
    protected ForecastCenterProxy centerProxy;

    @Mapping(target = "center", expression = "java(forecast.getCenterId() != null ? centerProxy.getCenterReference(forecast.getCenterId()) : null)")
    public abstract DemandForecastEntity toEntity(DemandForecast forecast);

    @Mapping(target = "centerId", source = "center.id")
    public abstract DemandForecast toDomain(DemandForecastEntity entity);

    public abstract DemandForecastResponse toResponse(DemandForecast forecast);
}
