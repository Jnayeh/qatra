package com.zayenha.qatra.analytics.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MetricsSnapshot {
    private Long id;
    private String metricName;
    private Double metricValue;
    private String dimensions;
    private Instant timestamp;

    public MetricsSnapshot() {}

    public MetricsSnapshot(String metricName, Double metricValue, String dimensions) {
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.dimensions = dimensions;
        this.timestamp = Instant.now();
    }
}
