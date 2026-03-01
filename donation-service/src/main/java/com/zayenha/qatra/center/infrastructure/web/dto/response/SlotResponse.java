package com.zayenha.qatra.center.infrastructure.web.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotResponse(
    Long id,
    Long centerId,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    int maxBookings,
    int maxRegularBookings,
    int bookedCount,
    int regularBookedCount,
    boolean isBlocked
) {}
