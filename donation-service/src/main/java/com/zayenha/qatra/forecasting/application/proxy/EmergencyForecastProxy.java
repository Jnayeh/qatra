package com.zayenha.qatra.forecasting.application.proxy;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.emergency.application.api.EmergencyForecastApi;
import com.zayenha.qatra.emergency.application.api.dto.DemandDataPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EmergencyForecastProxy {

    private final EmergencyForecastApi emergencyForecastApi;

    public List<DemandDataPoint> getHistoricalDemand(BloodType bloodType, int lookbackMonths) {
        return emergencyForecastApi.getHistoricalDemand(bloodType, lookbackMonths);
    }
}
