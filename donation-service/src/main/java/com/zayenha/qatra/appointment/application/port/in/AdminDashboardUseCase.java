package com.zayenha.qatra.appointment.application.port.in;

import java.time.LocalDate;
import java.util.List;

public interface AdminDashboardUseCase {

    DashboardStats getStats();

    List<CenterDonationSummary> getDonationSummaryByCenter();

    List<DailyDonationStats> getDailyDonationStats(LocalDate from, LocalDate to);

    record DashboardStats(
            long totalCompletedDonations,
            long totalMlCollected,
            long totalScheduled,
            long totalCancelled,
            long totalNoShows
    ) {}

    record CenterDonationSummary(
            Long centerId,
            long totalDonations,
            long totalMlCollected
    ) {}

    record DailyDonationStats(
            String date,
            long totalDonations,
            long totalMlCollected
    ) {}
}
