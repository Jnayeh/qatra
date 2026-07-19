package com.zayenha.qatra.donor.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileNudgeScheduler {

    private final DonorRepositoryPort donorRepository;
    private final EventPublisherPort eventPublisher;

    @Scheduled(cron = "${profile.nudge.cron:0 10 */4  * * *}")
    @Transactional(readOnly = true)
    public void sendProfileCompletionNudges() {
        var donors = donorRepository.findIncompleteProfiles();
        for (var donor : donors) {
            eventPublisher.publishProfileCompletionNudge(donor.getUserId());
            log.info("Sent profile completion nudge to donor userId={}", donor.getUserId());
        }
        if (!donors.isEmpty()) {
            log.info("ProfileNudgeScheduler: sent {} nudges", donors.size());
        }
    }
}
