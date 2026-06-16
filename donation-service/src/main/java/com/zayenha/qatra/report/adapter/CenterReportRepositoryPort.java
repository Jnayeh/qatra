package com.zayenha.qatra.report.adapter;

import com.zayenha.qatra.report.web.dto.CenterReportData;

import java.time.LocalDate;

public interface CenterReportRepositoryPort {
    CenterReportData getReportData(Long centerId, LocalDate startDate, LocalDate endDate);
}
