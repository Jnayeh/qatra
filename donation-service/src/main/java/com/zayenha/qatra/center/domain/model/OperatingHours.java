package com.zayenha.qatra.center.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public record OperatingHours(
    DaySchedule monday,
    DaySchedule tuesday,
    DaySchedule wednesday,
    DaySchedule thursday,
    DaySchedule friday,
    DaySchedule saturday,
    DaySchedule sunday,
    List<ClosureWindow> closedWindows
) {
    public record DaySchedule(
        LocalTime opens,
        LocalTime closes
    ) {}

    public record ClosureWindow(
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean allDay,
        String reason
    ) {}

    public Optional<DaySchedule> today() {
        return forDay(DayOfWeek.from(LocalDate.now(ZoneOffset.UTC)));
    }

    public Optional<DaySchedule> forDay(DayOfWeek day) {
        return Optional.ofNullable(switch (day) {
            case MONDAY    -> monday;
            case TUESDAY   -> tuesday;
            case WEDNESDAY -> wednesday;
            case THURSDAY  -> thursday;
            case FRIDAY    -> friday;
            case SATURDAY  -> saturday;
            case SUNDAY    -> sunday;
        });
    }
}
