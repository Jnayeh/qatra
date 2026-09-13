package com.zayenha.qatra.center.infrastructure.scheduler;

import com.zayenha.qatra.center.application.SlotGenerationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import io.micrometer.observation.annotation.Observed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationScheduler {

    private final SlotGenerationService slotGenerationService;

    @Value("${slot-generation.lookahead-days:21}")
    private int lookaheadDays;

    @Value("${slot-generation.default-period-minutes:60}")
    private int defaultSlotPeriodMinutes;

    @Value("${slot-generation.timezone:UTC}")
    private String timezone;

    @Value("${slot-generation.startup-max-retries:30}")
    private int startupMaxRetries;

    @Value("${slot-generation.startup-retry-delay-ms:3000}")
    private long startupRetryDelayMs;

    @PostConstruct
    public void init() {
        log.info("Running startup slot generation");
        for (int attempt = 1; attempt <= startupMaxRetries; attempt++) {
            try {
                slotGenerationService.generateSlots(lookaheadDays, defaultSlotPeriodMinutes, timezone);
                log.info("Startup slot generation succeeded on attempt {}", attempt);
                return;
            } catch (Exception e) {
                log.warn("Startup slot generation failed on attempt {}/{}: {}", attempt, startupMaxRetries, e.getMessage());
                if (attempt == startupMaxRetries) {
                    log.error("Startup slot generation failed after {} attempts. Continuing startup without slot generation.", startupMaxRetries);
                    return;
                }
                try {
                    Thread.sleep(startupRetryDelayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Startup slot generation retry interrupted. Continuing startup without slot generation.");
                    return;
                }
            }
        }
    }

    @Observed
    @Scheduled(cron = "${slot-generation.cron:0 0 0 */21 * *}", zone = "${slot-generation.timezone:UTC}")
    public void generateSlotsScheduled() {
        log.info("Running scheduled slot generation");
        slotGenerationService.generateSlots(lookaheadDays, defaultSlotPeriodMinutes, timezone);
    }
}
