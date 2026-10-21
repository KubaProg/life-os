package com.example.appbe.finance.bond;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

/** Statutory Polish holidays, including Christmas Eve from 2025; Saturdays are excluded. */
public final class PolishBusinessCalendar {
    private static final Set<MonthDay> FIXED = Set.of(MonthDay.of(1, 1), MonthDay.of(1, 6),
            MonthDay.of(5, 1), MonthDay.of(5, 3), MonthDay.of(8, 15), MonthDay.of(11, 1),
            MonthDay.of(11, 11), MonthDay.of(12, 25), MonthDay.of(12, 26));
    private PolishBusinessCalendar() {}
    public static boolean isBusinessDay(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || FIXED.contains(MonthDay.from(date))
                || (date.getYear() >= 2025 && MonthDay.from(date).equals(MonthDay.of(12, 24)))) return false;
        LocalDate easter = easter(date.getYear());
        return !date.equals(easter.plusDays(1)) && !date.equals(easter.plusDays(60));
    }
    public static LocalDate onOrAfter(LocalDate date) {
        while (!isBusinessDay(date)) date = date.plusDays(1);
        return date;
    }
    public static LocalDate addBusinessDays(LocalDate date, int days) {
        for (int i = 0; i < days; ) {
            date = date.plusDays(1);
            if (isBusinessDay(date)) i++;
        }
        return date;
    }
    private static LocalDate easter(int year) {
        int a = year % 19, b = year / 100, c = year % 100, d = b / 4, e = b % 4;
        int f = (b + 8) / 25, g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30, i = c / 4, k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7, m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        return LocalDate.of(year, month, (h + l - 7 * m + 114) % 31 + 1);
    }
}
