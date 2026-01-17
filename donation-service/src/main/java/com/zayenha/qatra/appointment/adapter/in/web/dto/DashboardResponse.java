package com.zayenha.qatra.appointment.adapter.in.web.dto;

import com.zayenha.qatra.appointment.application.port.in.AdminDashboardUseCase;

import java.util.List;

public record DashboardResponse(
        DashboardStatsResponse stats,
        List<CenterSummaryResponse> byCenter,
        List<DailyStatsResponse> dailyStats
) {
    public record DashboardStatsResponse(
            long totalCompletedDonations,
            long totalMlCollected,
            long totalScheduled,
            long totalCancelled,
            long totalNoShows
    ) {}

    public record CenterSummaryResponse(
            Long centerId,
            long totalDonations,
            long totalMlCollected
    ) {}

    public record DailyStatsResponse(
            String date,
            long totalDonations,
            long totalMlCollected
    ) {}

    public static DashboardResponse from(AdminDashboardUseCase.DashboardStats stats,
                                          List<AdminDashboardUseCase.CenterDonationSummary> byCenter,
                                          List<AdminDashboardUseCase.DailyDonationStats> dailyStats) {
        return new DashboardResponse(
                new DashboardStatsResponse(
                        stats.totalCompletedDonations(),
                        stats.totalMlCollected(),
                        stats.totalScheduled(),
                        stats.totalCancelled(),
                        stats.totalNoShows()
                ),
                byCenter.stream()
                        .map(c -> new CenterSummaryResponse(c.centerId(), c.totalDonations(), c.totalMlCollected()))
                        .toList(),
                dailyStats.stream()
                        .map(d -> new DailyStatsResponse(d.date(), d.totalDonations(), d.totalMlCollected()))
                        .toList()
        );
    }
}
