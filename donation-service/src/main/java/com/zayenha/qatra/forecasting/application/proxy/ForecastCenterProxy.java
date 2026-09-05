package com.zayenha.qatra.forecasting.application.proxy;

import com.zayenha.qatra.center.application.api.CenterApi;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ForecastCenterProxy {

    private final CenterApi centerApi;

    public CenterEntity getCenterReference(Long id) {
        return centerApi.getCenterReference(id);
    }
}
