package com.zayenha.qatra._shared.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public final class DateUtils {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    private DateUtils() {}

    public static Instant utcNow() {
        return Instant.now();
    }

    public static LocalDate utcToday() {
        return LocalDate.now(UTC);
    }

    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(UTC).toLocalDate();
    }

    public static Instant toStartOfDay(LocalDate date) {
        return date.atStartOfDay(UTC).toInstant();
    }

    public static Instant toEndOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay(UTC).toInstant().minus(1, ChronoUnit.MILLIS);
    }

    public static LocalDate plusDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    public static LocalDate minusDays(LocalDate date, long days) {
        return date.minusDays(days);
    }

    public static LocalDate plusMonths(LocalDate date, long months) {
        return date.plusMonths(months);
    }

    public static LocalDate minusMonths(LocalDate date, long months) {
        return date.minusMonths(months);
    }
}
