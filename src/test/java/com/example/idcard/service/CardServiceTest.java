package com.example.idcard.service;

import com.example.idcard.model.UserCard;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CardServiceTest {

    @Test
    void formatsSelectedMonthForCardDisplay() {
        YearMonth nextMonth = YearMonth.now().plusMonths(1);
        String expected = nextMonth.format(DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH));

        assertEquals(expected, CardService.normalizeValidityMonth(nextMonth.toString()));
    }

    @Test
    void defaultsMissingValidityMonthToNextMonth() {
        assertEquals(UserCard.defaultValidityMonth(), CardService.normalizeValidityMonth(null));
    }

    @Test
    void rejectsInvalidOrPastValidityMonths() {
        assertThrows(IllegalArgumentException.class,
                () -> CardService.normalizeValidityMonth("not-a-month"));
        assertThrows(IllegalArgumentException.class,
                () -> CardService.normalizeValidityMonth(YearMonth.now().minusMonths(1).toString()));
    }
}
