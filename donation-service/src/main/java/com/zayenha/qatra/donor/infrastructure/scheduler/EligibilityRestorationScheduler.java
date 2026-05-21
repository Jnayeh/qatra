package com.zayenha.qatra.donor.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.donor.domain.port.out.DonorRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EligibilityRestorationScheduler {

    private final DonorRepositoryPort donorRepository;
    private final EventPublisherPort eventPublisher;

    @Value("${eligibility.restoration.interval-ms:3600000}")
    private long restorationIntervalMs;

    @Scheduled(fixedRateString = "${eligibility.restoration.interval-ms:3600000}")
    @Transactional
    public void restoreEligibility() {
        var restored = donorRepository.findEligibilityRestoredDonors();
        for (var donor : restored) {
            var oldDate = donor.getEligibleFromDate();
            donor.setEligibleFromDate(null);
            donorRepository.save(donor);
            eventPublisher.publishEligibilityRestored(
                    donor.getUserId(), oldDate != null ? oldDate.toString() : null);
            log.info("Restored eligibility for donor userId={}", donor.getUserId());
        }
        log.info("EligibilityRestorationScheduler: restored {} donors", restored.size());
    }
}
