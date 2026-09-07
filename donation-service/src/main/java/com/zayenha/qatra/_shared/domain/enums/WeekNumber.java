package com.zayenha.qatra._shared.domain.enums;

public enum WeekNumber {
    FIRST(1, 7),
    SECOND(8, 15),
    THIRD(16, 21),
    FOURTH(22, -1);

    private final int startDay;
    private final int endDay;

    WeekNumber(int startDay, int endDay) {
        this.startDay = startDay;
        this.endDay = endDay;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static WeekNumber fromDay(int dayOfMonth) {
        if (dayOfMonth <= 7) return FIRST;
        if (dayOfMonth <= 15) return SECOND;
        if (dayOfMonth <= 21) return THIRD;
        return FOURTH;
    }

    public static int endDayOfMonth(java.time.YearMonth yearMonth) {
        return yearMonth.lengthOfMonth();
    }
}
