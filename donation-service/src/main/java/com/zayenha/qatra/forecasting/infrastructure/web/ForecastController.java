package com.zayenha.qatra.forecasting.infrastructure.web;

import com.zayenha.qatra._shared.web.ApiResponse;
import com.zayenha.qatra.forecasting.domain.port.in.ForecastQueryUseCases;
import com.zayenha.qatra.forecasting.infrastructure.mapper.ForecastMapper;
import com.zayenha.qatra.forecasting.infrastructure.web.dto.response.DemandForecastResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics/forecasts")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastQueryUseCases forecastQueryUseCases;
    private final ForecastMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CENTER_ADMIN')")
    public ResponseEntity<ApiResponse<List<DemandForecastResponse>>> getForecasts(
            @RequestParam(required = false) String bloodType,
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        var forecasts = forecastQueryUseCases.getForecasts(bloodType, centerId, city, country, year, month);
        var response = forecasts.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
