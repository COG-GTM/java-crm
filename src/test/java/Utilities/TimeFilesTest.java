package Utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TimeFilesTest {

    private static TimeZone originalTimeZone;

    @BeforeAll
    static void fixTimeZone() {
        originalTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @AfterAll
    static void restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone);
    }

    @Test
    void createLocalDateTimeParsesTwelveHourTime() {
        LocalDateTime ldt = TimeFiles.createLocalDateTime(LocalDate.of(2024, 3, 15), "1:30 PM");

        assertEquals(LocalDateTime.of(2024, 3, 15, 13, 30), ldt);
    }

    @Test
    void createLocalDateTimeRejectsUnparseableTime() {
        assertThrows(DateTimeParseException.class,
                () -> TimeFiles.createLocalDateTime(LocalDate.of(2024, 3, 15), "13:30"));
    }

    @Test
    void localDateTimeToDBStrConvertsFromLocalZoneToUtc() {
        // 2024-01-15 is EST (UTC-5)
        assertEquals("2024-01-15 17:30:00.0",
                TimeFiles.localDateTimeToDBStr(LocalDateTime.of(2024, 1, 15, 12, 30)));

        // 2024-07-15 is EDT (UTC-4)
        assertEquals("2024-07-15 16:30:00.0",
                TimeFiles.localDateTimeToDBStr(LocalDateTime.of(2024, 7, 15, 12, 30)));
    }

    @Test
    void dbStrToLocalDateTimeConvertsFromUtcToLocalZone() {
        assertEquals(LocalDateTime.of(2024, 1, 15, 12, 30),
                TimeFiles.dbStrToLocalDateTime("2024-01-15 17:30:00.0"));

        assertEquals(LocalDateTime.of(2024, 7, 15, 12, 30),
                TimeFiles.dbStrToLocalDateTime("2024-07-15 16:30:00.0"));
    }

    @Test
    void dbRoundTripPreservesTheInstant() {
        LocalDateTime original = LocalDateTime.of(2024, 11, 3, 1, 15);

        assertEquals(original, TimeFiles.dbStrToLocalDateTime(TimeFiles.localDateTimeToDBStr(original)));
    }

    @Test
    void uiFormattersUseTwelveHourAndSlashedDate() {
        LocalDateTime ldt = LocalDateTime.of(2024, 3, 5, 9, 5);

        assertEquals("9:05 AM", TimeFiles.localDateTimeToUITime(ldt));
        assertEquals("3/5/2024", TimeFiles.localDateTimeToUIDate(ldt));
    }

    @Test
    void availableAppointmentTimesCoverBusinessHoursInHalfHours() {
        List<String> times = TimeFiles.getAvailableAppointmentTimes();

        assertEquals(19, times.size());
        assertEquals("8:00 AM", times.get(0));
        assertEquals("5:00 PM", times.get(times.size() - 1));
        assertTrue(times.contains("12:30 PM"));
    }

    @Test
    void searchTimesForRollingYearEndAtTheGivenStart() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 10, 8, 0);

        List<String> searchTimes = TimeFiles.getSearchTimes(start, true);

        assertEquals(TimeFiles.localDateTimeToDBStr(start.minusYears(1)), searchTimes.get(0));
        assertEquals(TimeFiles.localDateTimeToDBStr(start), searchTimes.get(1));
    }

    @Test
    void searchTimesForCalendarYearSpanTheWholeYear() {
        LocalDateTime start = LocalDateTime.of(2024, 6, 10, 8, 0);

        List<String> searchTimes = TimeFiles.getSearchTimes(start, false);

        assertEquals(TimeFiles.localDateTimeToDBStr(LocalDateTime.of(2024, 1, 1, 0, 0)), searchTimes.get(0));
        assertEquals(TimeFiles.localDateTimeToDBStr(LocalDateTime.of(2024, 12, 31, 23, 59, 59, 999_999_999)),
                searchTimes.get(1));
    }
}
