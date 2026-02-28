package com.zayenha.qatra.center.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Slot {
    private Long id;
    private Long centerId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxBookings;
    private int maxRegularBookings;
    private int bookedCount;
    private int regularBookedCount;
    private boolean isBlocked;
    private Instant createdAt;

    public Slot() {}

    public Slot(Long centerId, LocalDate date, LocalTime startTime, LocalTime endTime,
                int maxBookings, int maxRegularBookings) {
        this.centerId = centerId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxBookings = maxBookings;
        this.maxRegularBookings = maxRegularBookings;
        this.bookedCount = 0;
        this.regularBookedCount = 0;
        this.isBlocked = false;
        this.createdAt = Instant.now();
    }

    public boolean isAvailable() {
        return !isBlocked && bookedCount < maxBookings;
    }

    public boolean isAvailableForRegular() {
        return isAvailable() && regularBookedCount < maxRegularBookings;
    }
}
