package com.zayenha.qatra.center.infrastructure.scheduler;

import com.zayenha.qatra.center.application.SlotGenerationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @PostConstruct
    public void init() {
        log.info("Running startup slot generation");
        slotGenerationService.generateSlots(lookaheadDays, defaultSlotPeriodMinutes, timezone);
    }

    @Scheduled(cron = "${slot-generation.cron:0 0 0 */21 * *}", zone = "${slot-generation.timezone:UTC}")
    public void generateSlotsScheduled() {
        log.info("Running scheduled slot generation");
        slotGenerationService.generateSlots(lookaheadDays, defaultSlotPeriodMinutes, timezone);
    }
}
