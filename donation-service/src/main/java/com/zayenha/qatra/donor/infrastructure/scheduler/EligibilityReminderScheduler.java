package com.zayenha.qatra.donor.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityReminderScheduler {

    private final DonorRepositoryPort donorRepository;
    private final EventPublisherPort eventPublisher;

    @Scheduled(cron = "${eligibility.reminder.cron:0 0 9 * * *}")
    public void sendEligibilityReminders() {
        var today = LocalDate.now();
        var donors = donorRepository.findByEligibleFromDate(today);
        for (var donor : donors) {
            eventPublisher.publishNotificationDispatch(
                    donor.getUserId(), null, "ELIGIBILITY_REMINDER",
                    "You Can Donate Again",
                    "Your eligibility to donate blood has been restored. Schedule your next donation today!",
                    Map.of("eligibleFromDate", today.toString()));
            log.info("Sent eligibility reminder to donor userId={}", donor.getUserId());
        }
        if (!donors.isEmpty()) {
            log.info("EligibilityReminderScheduler: sent {} reminders", donors.size());
        }
    }
}
