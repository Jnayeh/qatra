package com.zayenha.qatra.forecasting.application;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.emergency.application.api.dto.DemandDataPoint;
import com.zayenha.qatra.forecasting.application.proxy.EmergencyForecastProxy;
import com.zayenha.qatra.forecasting.domain.model.DemandForecast;
import com.zayenha.qatra.forecasting.domain.model.ForecastPeriodType;
import com.zayenha.qatra.forecasting.domain.port.in.ForecastQueryUseCases;
import com.zayenha.qatra.forecasting.domain.port.out.DemandForecastRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastingService implements ForecastQueryUseCases {

    private final EmergencyForecastProxy emergencyForecastProxy;
    private final DemandForecastRepositoryPort demandForecastRepositoryPort;

    private static final int LOOKBACK_MONTHS = 6;
    private static final int MIN_DATA_POINTS = 2;

    @Override
    @Transactional(readOnly = true)
    public List<DemandForecast> getForecasts(String bloodType, Long centerId, String city, String country, Integer year, Integer month) {
        return demandForecastRepositoryPort.findFiltered(bloodType, centerId, city, country, year, month);
    }

    @Transactional
    public void generateForecasts() {
        var nextMonth = LocalDate.now().plusMonths(1);
        var targetYear = nextMonth.getYear();
        var targetMonth = nextMonth.getMonthValue();

        log.info("Generating forecasts for {}/{}", targetYear, targetMonth);

        demandForecastRepositoryPort.deleteByYearAndMonth(targetYear, targetMonth);

        var historicalData = emergencyForecastProxy.getHistoricalDemand(null, LOOKBACK_MONTHS);

        if (historicalData.isEmpty()) {
            log.info("No historical emergency data found, skipping forecast generation");
            return;
        }

        var forecasts = new ArrayList<DemandForecast>();

        var bloodTypes = historicalData.stream()
                .map(d -> d.getBloodType())
                .distinct()
                .toList();

        for (var bt : bloodTypes) {
            if (bt == BloodType.UNKNOWN) continue;

            var btData = historicalData.stream()
                    .filter(d -> d.getBloodType() == bt)
                    .toList();

            generateCenterForecasts(btData, bt, targetYear, targetMonth, forecasts);
            generateCityForecasts(btData, bt, targetYear, targetMonth, forecasts);
            generateCountryForecasts(btData, bt, targetYear, targetMonth, forecasts);
        }

        if (!forecasts.isEmpty()) {
            demandForecastRepositoryPort.saveAll(forecasts);
            log.info("Generated {} forecast records for {}/{}", forecasts.size(), targetYear, targetMonth);
        }
    }

    private void generateCenterForecasts(List<DemandDataPoint> data, BloodType bt,
                                         int year, int month, List<DemandForecast> forecasts) {
        var byCenter = data.stream()
                .filter(d -> d.getCenterId() != null)
                .collect(Collectors.groupingBy(DemandDataPoint::getCenterId));

        for (var entry : byCenter.entrySet()) {
            var centerData = entry.getValue();
            var first = centerData.get(0);
            computeAndAdd(centerData, bt, entry.getKey(), first.getCenterCity(), first.getCenterCountry(),
                    year, month, forecasts);
        }
    }

    private void generateCityForecasts(List<DemandDataPoint> data, BloodType bt,
                                       int year, int month, List<DemandForecast> forecasts) {
        var byCity = data.stream()
                .filter(d -> d.getCenterCity() != null && !d.getCenterCity().isBlank())
                .collect(Collectors.groupingBy(DemandDataPoint::getCenterCity));

        for (var entry : byCity.entrySet()) {
            computeAndAdd(entry.getValue(), bt, null, entry.getKey(), null,
                    year, month, forecasts);
        }
    }

    private void generateCountryForecasts(List<DemandDataPoint> data, BloodType bt,
                                          int year, int month, List<DemandForecast> forecasts) {
        var byCountry = data.stream()
                .filter(d -> d.getCenterCountry() != null && !d.getCenterCountry().isBlank())
                .collect(Collectors.groupingBy(DemandDataPoint::getCenterCountry));

        for (var entry : byCountry.entrySet()) {
            computeAndAdd(entry.getValue(), bt, null, null, entry.getKey(),
                    year, month, forecasts);
        }
    }

    private void computeAndAdd(List<DemandDataPoint> groupData, BloodType bt,
                               Long centerId, String city, String country,
                               int year, int month, List<DemandForecast> forecasts) {
        var now = Instant.now();

        for (int weekNum = 1; weekNum <= 4; weekNum++) {
            int finalWeekNum = weekNum;
            var weekData = groupData.stream()
                    .filter(d -> d.getWeekNumber() != null && d.getWeekNumber() == finalWeekNum)
                    .toList();

            double avgDemand = weekData.stream()
                    .mapToInt(DemandDataPoint::getTotalUnits)
                    .average()
                    .orElse(0.0);

            double confidence = weekData.isEmpty() ? 0.0
                    : Math.min(weekData.size() / (double) LOOKBACK_MONTHS, 1.0);

            forecasts.add(new DemandForecast(null, bt, centerId, city, country,
                    ForecastPeriodType.WEEK, weekNum, year, month,
                    Math.round(avgDemand * 100.0) / 100.0, confidence, now));
        }

        var monthlyTotals = new LinkedHashMap<String, Integer>();
        for (var d : groupData) {
            var monthKey = d.getYear() + "-" + d.getMonth();
            monthlyTotals.merge(monthKey, d.getTotalUnits(), Integer::sum);
        }

        double avgMonthly = monthlyTotals.values().stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        double monthlyConfidence = monthlyTotals.isEmpty() ? 0.0
                : Math.min(monthlyTotals.size() / (double) LOOKBACK_MONTHS, 1.0);

        forecasts.add(new DemandForecast(null, bt, centerId, city, country,
                ForecastPeriodType.MONTH, null, year, month,
                Math.round(avgMonthly * 100.0) / 100.0, monthlyConfidence, now));
    }
}
