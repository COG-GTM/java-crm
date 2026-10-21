package Utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeFilesTest {

    private TimeZone originalZone;

    @BeforeEach
    void pinUserZone() {
        originalZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    }

    @AfterEach
    void restoreUserZone() {
        TimeZone.setDefault(originalZone);
    }

    @Test
    void localDateTimeToDbTimestamp_convertsUserWallClockToInstant() {
        // 9:30 Eastern Standard Time is 14:30 UTC
        LocalDateTime userLocal = LocalDateTime.of(2021, 1, 15, 9, 30);

        Timestamp ts = TimeFiles.localDateTimeToDbTimestamp(userLocal);

        assertEquals(Instant.parse("2021-01-15T14:30:00Z"), ts.toInstant());
    }

    @Test
    void localDateTimeToDbTimestamp_honoursDaylightSavingTime() {
        // 9:30 Eastern Daylight Time is 13:30 UTC
        LocalDateTime userLocal = LocalDateTime.of(2021, 7, 15, 9, 30);

        Timestamp ts = TimeFiles.localDateTimeToDbTimestamp(userLocal);

        assertEquals(Instant.parse("2021-07-15T13:30:00Z"), ts.toInstant());
    }

    @Test
    void dbTimestampToLocalDateTime_roundTripsThroughUtc() {
        LocalDateTime userLocal = LocalDateTime.of(2021, 3, 14, 9, 30);

        LocalDateTime back = TimeFiles.dbTimestampToLocalDateTime(TimeFiles.localDateTimeToDbTimestamp(userLocal));

        assertEquals(userLocal, back);
    }

    @Test
    void dbTimestampToLocalDateTime_isNullSafe() {
        assertNull(TimeFiles.dbTimestampToLocalDateTime(null));
    }

    @Test
    void utcCalendar_isAnchoredToUtc() {
        assertEquals(ZoneOffset.UTC, TimeFiles.utcCalendar().getTimeZone().toZoneId().normalized());
        assertEquals(ZoneId.of("UTC"), TimeFiles.utcCalendar().getTimeZone().toZoneId());
    }

    @Test
    void getSearchTimes_rollingYear_returnsOneYearWindowEndingAtStart() {
        LocalDateTime start = LocalDateTime.of(2021, 6, 15, 10, 0);

        List<LocalDateTime> window = TimeFiles.getSearchTimes(start, true);

        assertEquals(List.of(start.minusYears(1), start), window);
    }

    @Test
    void getSearchTimes_calendarYear_spansWholeYearOfStart() {
        LocalDateTime start = LocalDateTime.of(2020, 6, 15, 10, 0); // leap year

        List<LocalDateTime> window = TimeFiles.getSearchTimes(start, false);

        assertEquals(LocalDateTime.of(2020, 1, 1, 0, 0), window.get(0));
        assertEquals(LocalDateTime.of(2020, 12, 31, 0, 0).with(LocalTime.MAX), window.get(1));
    }
}
