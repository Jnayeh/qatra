package com.zayenha.qatra.analytics.infrastructure.persistence.repository;

import com.zayenha.qatra.analytics.infrastructure.persistence.entity.MetricsSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface MetricsSnapshotJpaRepository extends JpaRepository<MetricsSnapshotEntity, Long> {
    List<MetricsSnapshotEntity> findByMetricNameAndTimestampBetweenOrderByTimestampAsc(String metricName, Instant from, Instant to);
    List<MetricsSnapshotEntity> findByMetricNameOrderByTimestampDesc(String metricName);
}
