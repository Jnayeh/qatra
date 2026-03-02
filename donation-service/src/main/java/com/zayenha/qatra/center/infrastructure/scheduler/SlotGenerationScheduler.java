package com.zayenha.qatra.center.infrastructure.scheduler;

import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.infrastructure.persistence.adapter.CenterRepositoryAdapter;
import com.zayenha.qatra.center.infrastructure.persistence.adapter.SlotRepositoryAdapter;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationScheduler {

    private final CenterRepositoryAdapter centerRepositoryAdapter;
    private final SlotRepositoryAdapter slotRepositoryAdapter;

    @PostConstruct
    public void init() {
        log.info("Running startup slot generation");
        generateSlots();
    }

    @Scheduled(cron = "0 0 0 */21 * *", zone = "UTC")
    @Transactional
    public void generateSlotsScheduled() {
        log.info("Running scheduled slot generation");
        generateSlots();
    }

    @Transactional
    public void generateSlots() {
        var today = LocalDate.now(java.time.ZoneOffset.UTC);
        var end = today.plusDays(21);
        var result = centerRepositoryAdapter.findAll(SearchCriteria.defaultAll());

        for (var center : result.content()) {
            if (center.getStatus() != com.zayenha.qatra.center.domain.model.CenterStatus.ACTIVE) continue;
            var hours = center.getOperatingHours();
            if (hours == null) continue;
            var existingSlots = slotRepositoryAdapter.findAllByDateRange(today, end);
            var existingKeys = existingSlots.stream()
                    .map(s -> s.getCenterId() + ":" + s.getDate() + ":" + s.getStartTime())
                    .toList();

            for (int i = 0; i <= 21; i++) {
                var date = today.plusDays(i);
                var dayScheduleOpt = hours.forDay(DayOfWeek.from(date));
                if (dayScheduleOpt.isEmpty()) continue;

                var daySchedule = dayScheduleOpt.get();
                var slotPeriod = center.getSlotPeriod() != null ? center.getSlotPeriod() : 60;
                var start = daySchedule.open();
                var endTime = daySchedule.close();

                while (start.plusMinutes(slotPeriod).isBefore(endTime) || start.plusMinutes(slotPeriod).equals(endTime)) {
                    var slotEnd = start.plusMinutes(slotPeriod);
                    var key = center.getId() + ":" + date + ":" + start;
                    if (!existingKeys.contains(key) && !isClosed(hours, date, start, slotEnd)) {
                        var slot = new Slot(center.getId(), date, start, slotEnd,
                                center.getTotalCapacity(), center.getMaxRegular());
                        slotRepositoryAdapter.save(slot, center.getId());
                    }
                    start = slotEnd;
                }
            }
        }
        log.info("Slot generation complete for {} centers", result.content().size());
    }

    private boolean isClosed(OperatingHours hours, LocalDate date, LocalTime start, LocalTime end) {
        if (hours.closedWindows() == null) return false;
        return hours.closedWindows().stream().anyMatch(w -> {
            if (!w.date().equals(date)) return false;
            if (w.allDay()) return true;
            if (w.startTime() == null || w.endTime() == null) return false;
            return start.isBefore(w.endTime()) && end.isAfter(w.startTime());
        });
    }
}
