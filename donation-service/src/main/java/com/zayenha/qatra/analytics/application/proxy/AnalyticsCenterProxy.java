package com.zayenha.qatra.analytics.application.proxy;

import com.zayenha.qatra.center.application.api.CenterApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsCenterProxy {

    private final CenterApi centerApi;

    public long countActiveCenters() {
        return centerApi.countActiveCenters();
    }
}