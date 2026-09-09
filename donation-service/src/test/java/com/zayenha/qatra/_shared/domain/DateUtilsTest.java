package com.zayenha.qatra._shared.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    private static final ZoneOffset UTC = ZoneOffset.UTC;

    // ── utcNow ──────────────────────────────────────────────────────────

    @Test
    void utcNow_returnsInstant() {
        Instant before = Instant.now().minusSeconds(2);
        Instant result = DateUtils.utcNow();
        Instant after = Instant.now().plusSeconds(2);

        assertNotNull(result);
        assertTrue(result.isAfter(before));
        assertTrue(result.isBefore(after));
    }

    @Test
    void utcNow_isTrulyUtc() {
        Instant result = DateUtils.utcNow();
        ZonedDateTime zoned = result.atZone(UTC);
        assertEquals(UTC, zoned.getOffset());
    }

    // ── utcToday ────────────────────────────────────────────────────────

    @Test
    void utcToday_returnsLocalDate() {
        LocalDate result = DateUtils.utcToday();
        LocalDate expected = LocalDate.now(UTC);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void utcToday_matchesInstantUtc() {
        LocalDate fromMethod = DateUtils.utcToday();
        LocalDate fromInstant = DateUtils.toLocalDate(DateUtils.utcNow());

        assertEquals(fromInstant, fromMethod);
    }

    // ── toLocalDate ─────────────────────────────────────────────────────

    @Test
    void toLocalDate_midnightInstant() {
        Instant midnight = LocalDate.of(2025, 6, 15)
                .atStartOfDay(UTC).toInstant();

        assertEquals(LocalDate.of(2025, 6, 15), DateUtils.toLocalDate(midnight));
    }

    @Test
    void toLocalDate_endOfDayInstant() {
        Instant endOfDay = LocalDate.of(2025, 6, 15)
                .plusDays(1).atStartOfDay(UTC).toInstant().minus(1, ChronoUnit.MILLIS);

        assertEquals(LocalDate.of(2025, 6, 15), DateUtils.toLocalDate(endOfDay));
    }

    @Test
    void toLocalDate_otherOffset_ignored() {
        Instant sameInstantDifferentOffset = LocalDateTime.of(2025, 6, 15, 18, 30)
                .atOffset(ZoneOffset.ofHours(1)).toInstant();

        // 2025-06-15T18:30+01:00 = 2025-06-15T17:30Z → still 2025-06-15 in UTC
        assertEquals(LocalDate.of(2025, 6, 15), DateUtils.toLocalDate(sameInstantDifferentOffset));
    }

    @Test
    void toLocalDate_crossesMidnightInUtc() {
        // 2025-06-15T23:30Z → UTC date is 2025-06-15
        Instant justBeforeMidnight = LocalDateTime.of(2025, 6, 15, 23, 30)
                .atOffset(UTC).toInstant();
        assertEquals(LocalDate.of(2025, 6, 15), DateUtils.toLocalDate(justBeforeMidnight));
    }

    @Test
    void toLocalDate_crossesMidnightInUtc_nextDay() {
        // 2025-06-16T00:30Z → UTC date is 2025-06-16
        Instant justAfterMidnight = LocalDateTime.of(2025, 6, 16, 0, 30)
                .atOffset(UTC).toInstant();
        assertEquals(LocalDate.of(2025, 6, 16), DateUtils.toLocalDate(justAfterMidnight));
    }

    // ── toStartOfDay ────────────────────────────────────────────────────

    @Test
    void toStartOfDay_returnsMidnightUtc() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        Instant result = DateUtils.toStartOfDay(date);

        assertEquals(LocalDateTime.of(2025, 3, 10, 0, 0, 0), result.atZone(UTC).toLocalDateTime());
    }

    @Test
    void toStartOfDay_roundTrip() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        Instant start = DateUtils.toStartOfDay(date);
        LocalDate roundTrip = DateUtils.toLocalDate(start);

        assertEquals(date, roundTrip);
    }

    // ── toEndOfDay ──────────────────────────────────────────────────────

    @Test
    void toEndOfDay_isOneMillisecondBeforeNextDay() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        Instant result = DateUtils.toEndOfDay(date);

        Instant expected = LocalDateTime.of(2025, 3, 11, 0, 0, 0)
                .minus(1, ChronoUnit.MILLIS).atOffset(UTC).toInstant();
        assertEquals(expected, result);
    }

    @Test
    void toEndOfDay_toLocalDateStillSameDay() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        Instant endOfDay = DateUtils.toEndOfDay(date);

        assertEquals(date, DateUtils.toLocalDate(endOfDay));
    }

    @Test
    void toEndOfDay_isAfterStartOfDay() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        Instant start = DateUtils.toStartOfDay(date);
        Instant end = DateUtils.toEndOfDay(date);

        assertTrue(end.isAfter(start));
    }

    @Test
    void toEndOfDay_isExactlyOneDayMinusOneMsFromStart() {
        LocalDate date = LocalDate.of(2025, 3, 10);
        Instant start = DateUtils.toStartOfDay(date);
        Instant end = DateUtils.toEndOfDay(date);

        long millis = ChronoUnit.MILLIS.between(start, end);
        assertEquals(24 * 60 * 60 * 1000L - 1, millis);
    }

    @Test
    void toEndOfDay_leapYearBoundary() {
        LocalDate feb28 = LocalDate.of(2025, 2, 28);
        Instant end = DateUtils.toEndOfDay(feb28);

        assertEquals(feb28, DateUtils.toLocalDate(end));
    }

    @Test
    void toEndOfDay_yearBoundary() {
        LocalDate dec31 = LocalDate.of(2025, 12, 31);
        Instant end = DateUtils.toEndOfDay(dec31);

        assertEquals(dec31, DateUtils.toLocalDate(end));
        Instant startNextYear = DateUtils.toStartOfDay(LocalDate.of(2026, 1, 1));
        assertEquals(startNextYear.toEpochMilli() - 1, end.toEpochMilli());
    }

    // ── plusDays / minusDays ────────────────────────────────────────────

    @Test
    void plusDays_forward() {
        LocalDate date = LocalDate.of(2025, 6, 1);
        assertEquals(LocalDate.of(2025, 6, 8), DateUtils.plusDays(date, 7));
    }

    @Test
    void plusDays_backward() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(LocalDate.of(2025, 6, 1), DateUtils.plusDays(date, -14));
    }

    @Test
    void minusDays_forward() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(LocalDate.of(2025, 6, 1), DateUtils.minusDays(date, 14));
    }

    @Test
    void minusDays_backward() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertEquals(LocalDate.of(2024, 12, 1), DateUtils.minusDays(date, 31));
    }

    @Test
    void plusDays_zeroIsIdentity() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(date, DateUtils.plusDays(date, 0));
    }

    @Test
    void minusDays_zeroIsIdentity() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(date, DateUtils.minusDays(date, 0));
    }

    // ── plusMonths / minusMonths ────────────────────────────────────────

    @Test
    void plusMonths_forward() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        assertEquals(LocalDate.of(2025, 7, 15), DateUtils.plusMonths(date, 6));
    }

    @Test
    void plusMonths_crossYear() {
        LocalDate date = LocalDate.of(2025, 10, 15);
        assertEquals(LocalDate.of(2026, 4, 15), DateUtils.plusMonths(date, 6));
    }

    @Test
    void plusMonths_endOfMonth_clampsToValidDay() {
        LocalDate jan31 = LocalDate.of(2025, 1, 31);
        assertEquals(LocalDate.of(2025, 2, 28), DateUtils.plusMonths(jan31, 1));
    }

    @Test
    void minusMonths_forward() {
        LocalDate date = LocalDate.of(2025, 12, 15);
        assertEquals(LocalDate.of(2025, 6, 15), DateUtils.minusMonths(date, 6));
    }

    @Test
    void minusMonths_crossYear() {
        LocalDate date = LocalDate.of(2025, 4, 15);
        assertEquals(LocalDate.of(2024, 10, 15), DateUtils.minusMonths(date, 6));
    }

    @Test
    void minusMonths_endOfMonth_clampsToValidDay() {
        LocalDate mar31 = LocalDate.of(2025, 3, 31);
        assertEquals(LocalDate.of(2025, 2, 28), DateUtils.minusMonths(mar31, 1));
    }

    @Test
    void plusMonths_zeroIsIdentity() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(date, DateUtils.plusMonths(date, 0));
    }

    @Test
    void minusMonths_zeroIsIdentity() {
        LocalDate date = LocalDate.of(2025, 6, 15);
        assertEquals(date, DateUtils.minusMonths(date, 0));
    }

    // ── edge cases ──────────────────────────────────────────────────────

    @Test
    void toStartOfDay_epochDate() {
        Instant result = DateUtils.toStartOfDay(LocalDate.of(1970, 1, 1));
        assertEquals(Instant.EPOCH, result);
    }

    @Test
    void toEndOfDay_epochDate() {
        Instant result = DateUtils.toEndOfDay(LocalDate.of(1970, 1, 1));
        assertEquals(Instant.EPOCH.toEpochMilli() + 24 * 60 * 60 * 1000L - 1, result.toEpochMilli());
    }

    @Test
    void plusDays_largeDuration() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        LocalDate result = DateUtils.plusDays(date, 364);
        assertEquals(LocalDate.of(2025, 12, 31), result);
    }

    @Test
    void minusDays_largeDuration() {
        LocalDate date = LocalDate.of(2025, 12, 31);
        LocalDate result = DateUtils.minusDays(date, 364);
        assertEquals(LocalDate.of(2025, 1, 1), result);
    }

    @Test
    void plusMonths_fullYear() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        assertEquals(LocalDate.of(2026, 1, 1), DateUtils.plusMonths(date, 12));
    }

    @Test
    void minusMonths_fullYear() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        assertEquals(LocalDate.of(2025, 1, 1), DateUtils.minusMonths(date, 12));
    }
}
