package com.zayenha.qatra.center.application.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlotDTO {
    private Long id;
    private Long centerId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int bookedCount;
    private int regularBookedCount;
    private int maxBookings;
    private int maxRegularBookings;
    private boolean isBlocked;
}
