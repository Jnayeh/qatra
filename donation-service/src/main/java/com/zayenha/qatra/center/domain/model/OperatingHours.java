package com.zayenha.qatra.center.domain.model;

import jakarta.annotation.Nullable;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

public record OperatingHours(
    @Nullable DaySchedule monday,
    @Nullable DaySchedule tuesday,
    @Nullable DaySchedule wednesday,
    @Nullable DaySchedule thursday,
    @Nullable DaySchedule friday,
    @Nullable DaySchedule saturday,
    @Nullable DaySchedule sunday,
    @Nullable List<ClosureWindow> closedWindows
) {
    public record DaySchedule(
        LocalTime open,
        LocalTime close
    ) {}

    public record ClosureWindow(
        LocalDate date,
        @Nullable LocalTime startTime,
        @Nullable LocalTime endTime,
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
