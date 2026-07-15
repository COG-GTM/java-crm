package Utilities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.TimeZone;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link TimeFiles} (Group D — Utilities).
 *
 * <p>The default {@link TimeZone} is pinned to UTC for each test and restored
 * afterwards so the UTC-based DB conversions are deterministic and the
 * hard-coded string assertions hold regardless of the host machine's zone.</p>
 */
class TimeFilesTest {

    private TimeZone originalZone;

    @BeforeEach
    void fixTimeZone() {
        originalZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @AfterEach
    void restoreTimeZone() {
        TimeZone.setDefault(originalZone);
    }

    @Test
    void createLocalDateTimeCombinesDateAndTwelveHourTime() {
        LocalDate date = LocalDate.of(2021, 3, 5);
        LocalDateTime ldt = TimeFiles.createLocalDateTime(date, "1:30 PM");
        assertEquals(LocalDateTime.of(2021, 3, 5, 13, 30), ldt);
    }

    @Test
    void dbStringRoundTripReturnsOriginalLocalDateTime() {
        LocalDateTime original = LocalDateTime.of(2021, 6, 15, 10, 30, 0);
        String dbStr = TimeFiles.localDateTimeToDBStr(original);
        assertEquals(original, TimeFiles.dbStrToLocalDateTime(dbStr));
    }

    @Test
    void localDateTimeToUITimeFormatsTwelveHourClock() {
        LocalDateTime ldt = LocalDateTime.of(2021, 1, 1, 13, 30);
        assertEquals("1:30 PM", TimeFiles.localDateTimeToUITime(ldt));
    }

    @Test
    void localDateTimeToUIDateFormatsMonthDayYear() {
        LocalDateTime ldt = LocalDateTime.of(2021, 3, 5, 9, 0);
        assertEquals("3/5/2021", TimeFiles.localDateTimeToUIDate(ldt));
    }

    @Test
    void getAvailableAppointmentTimesCoversBusinessHoursInHalfHours() {
        ObservableList<String> times = TimeFiles.getAvailableAppointmentTimes();
        assertEquals(19, times.size());
        assertEquals("8:00 AM", times.get(0));
        assertEquals("5:00 PM", times.get(times.size() - 1));
    }

    @Test
    void dbStrNowIsNonNullAndParseable() {
        String now = TimeFiles.dbStrNow();
        assertNotNull(now);
        assertNotNull(TimeFiles.dbStrToLocalDateTime(now));
    }

    @Test
    void getSearchTimesRollingReturnsOneYearWindowEndingAtStart() {
        LocalDateTime start = LocalDateTime.of(2021, 6, 15, 10, 30, 0);
        ObservableList<String> searchTimes = TimeFiles.getSearchTimes(start, true);

        assertEquals(2, searchTimes.size());
        assertEquals(start.minusYears(1),
                TimeFiles.dbStrToLocalDateTime(searchTimes.get(0)));
        assertEquals(start,
                TimeFiles.dbStrToLocalDateTime(searchTimes.get(1)));
    }

    @Test
    void getSearchTimesCalendarYearReturnsFullYearWindow() {
        LocalDateTime start = LocalDateTime.of(2021, 6, 15, 10, 30, 0);
        ObservableList<String> searchTimes = TimeFiles.getSearchTimes(start, false);

        assertEquals(2, searchTimes.size());
        // With the default TimeZone pinned to UTC, the DB strings equal the
        // formatted first/last instants of the calendar year directly.
        assertEquals("2021-01-01 00:00:00.0", searchTimes.get(0));
        assertEquals("2021-12-31 23:59:59.9", searchTimes.get(1));

        // Sanity-check the intent: start of Jan 1 and end of Dec 31.
        LocalDateTime firstOfYear = TimeFiles.dbStrToLocalDateTime(searchTimes.get(0));
        assertEquals(LocalDateTime.of(2021, 1, 1, 0, 0, 0), firstOfYear);
        assertEquals(LocalDate.of(2021, 12, 31),
                TimeFiles.dbStrToLocalDateTime(searchTimes.get(1)).toLocalDate());
        assertEquals(LocalTime.of(23, 59, 59),
                TimeFiles.dbStrToLocalDateTime(searchTimes.get(1)).toLocalTime().withNano(0));
    }
}
