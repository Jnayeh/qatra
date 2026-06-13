package com.zayenha.qatra.analytics.domain.port.out;

import com.zayenha.qatra.analytics.domain.model.CenterMetrics;

public interface CenterMetricsRepositoryPort {
    CenterMetrics getMetrics(Long centerId);
}
