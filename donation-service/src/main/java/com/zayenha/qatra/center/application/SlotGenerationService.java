package com.zayenha.qatra.center.application;

import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.center.domain.model.Slot;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.port.out.SlotRepositoryPort;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationService {

    private final CenterRepositoryPort centerRepository;
    private final SlotRepositoryPort slotRepository;

    @Transactional
    public void generateSlots(int lookaheadDays, int defaultSlotPeriodMinutes, String timezone) {
        var today = LocalDate.now(ZoneId.of(timezone));
        var end = today.plusDays(lookaheadDays);
        var result = centerRepository.findAll(SearchCriteria.defaultAll());

        for (var center : result.content()) {
            if (center.getStatus() != com.zayenha.qatra.center.domain.model.CenterStatus.ACTIVE) continue;
            var hours = center.getOperatingHours();
            if (hours == null) continue;
            var existingSlots = slotRepository.findAllByDateRange(today, end);
            var existingKeys = existingSlots.stream()
                    .map(s -> s.getCenterId() + ":" + s.getDate() + ":" + s.getStartTime())
                    .toList();

            for (int i = 0; i <= lookaheadDays; i++) {
                var date = today.plusDays(i);
                var dayScheduleOpt = hours.forDay(DayOfWeek.from(date));
                if (dayScheduleOpt.isEmpty()) continue;

                var daySchedule = dayScheduleOpt.get();
                var slotPeriod = center.getSlotPeriod() != null ? center.getSlotPeriod() : defaultSlotPeriodMinutes;
                var start = daySchedule.opens();
                var endTime = daySchedule.closes();

                while (start.plusMinutes(slotPeriod).isBefore(endTime) || start.plusMinutes(slotPeriod).equals(endTime)) {
                    var slotEnd = start.plusMinutes(slotPeriod);
                    var key = center.getId() + ":" + date + ":" + start;
                    if (!existingKeys.contains(key) && !isClosed(hours, date, start, slotEnd)) {
                        var slot = new Slot(center.getId(), date, start, slotEnd,
                                center.getTotalCapacity(), center.getMaxRegular());
                        slotRepository.save(slot, center.getId());
                    }
                    start = slotEnd;
                }
            }
            log.info("Slot generation complete for center {}", center.getId());
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
