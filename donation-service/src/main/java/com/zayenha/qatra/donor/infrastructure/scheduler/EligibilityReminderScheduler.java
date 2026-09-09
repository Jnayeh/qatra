package com.zayenha.qatra.donor.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.DateUtils;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.observation.annotation.Observed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityReminderScheduler {

    private final DonorRepositoryPort donorRepository;
    private final EventPublisherPort eventPublisher;

    @Observed
    @Scheduled(cron = "${eligibility.reminder.cron:0 0 9 * * *}")
    public void sendEligibilityReminders() {
        var today = DateUtils.utcToday();
        var donors = donorRepository.findByEligibleFromDate(today);
        for (var donor : donors) {
            eventPublisher.publishEligibilityReminder(
                    donor.getUserId(), today.toString());
            log.info("Sent eligibility reminder to donor userId={}", donor.getUserId());
        }
        if (!donors.isEmpty()) {
            log.info("EligibilityReminderScheduler: sent {} reminders", donors.size());
        }
    }
}
