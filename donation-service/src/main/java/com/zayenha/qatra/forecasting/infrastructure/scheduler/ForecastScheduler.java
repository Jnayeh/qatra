package com.zayenha.qatra.forecasting.infrastructure.scheduler;

import com.zayenha.qatra.forecasting.application.ForecastingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.observation.annotation.Observed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForecastScheduler {

    private final ForecastingService forecastingService;

    @Observed
    @Scheduled(cron = "${forecast.generation.cron:0 0 3 1 * *}", zone = "${forecast.generation.timezone:UTC}")
    public void generateForecasts() {
        log.info("ForecastScheduler: starting monthly forecast generation");
        try {
            forecastingService.generateForecasts();
            log.info("ForecastScheduler: forecast generation completed");
        } catch (Exception e) {
            log.error("ForecastScheduler: forecast generation failed", e);
        }
    }
}
