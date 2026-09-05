package com.zayenha.qatra.analytics.application.proxy;

import com.zayenha.qatra.emergency.application.api.EmergencyApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsEmergencyProxy {

    private final EmergencyApi emergencyApi;

    public long countDonorResponses() {
        return emergencyApi.countDonorResponses();
    }

    public long countDonorResponsesAccepted() {
        return emergencyApi.countDonorResponsesAccepted();
    }
}