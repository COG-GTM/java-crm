package DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GeneralDaoImplTest {

    private static final LocalDateTime CREATED = LocalDateTime.of(2021, 1, 1, 8, 0);
    private static final LocalDateTime UPDATED = LocalDateTime.of(2021, 2, 2, 9, 0);

    private static Timestamp asDbTimestamp(LocalDateTime userLocal) {
        return Timestamp.from(userLocal.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Test
    void getMetadata_readsAuditTimestampsViaUtcCalendar() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("createdBy")).thenReturn("alice");
        when(rs.getString("lastUpdateBy")).thenReturn("bob");
        when(rs.getTimestamp(eq("createDate"), any(Calendar.class))).thenReturn(asDbTimestamp(CREATED));
        when(rs.getTimestamp(eq("lastUpdate"), any(Calendar.class))).thenReturn(asDbTimestamp(UPDATED));

        GeneralDaoImpl.getMetadata(rs);

        assertEquals("alice", GeneralDaoImpl.createdBy);
        assertEquals("bob", GeneralDaoImpl.lastUpdateBy);
        assertEquals(CREATED, GeneralDaoImpl.createDateLdt);
        assertEquals(UPDATED, GeneralDaoImpl.lastUpdateLdt);

        ArgumentCaptor<Calendar> cal = ArgumentCaptor.forClass(Calendar.class);
        verify(rs).getTimestamp(eq("createDate"), cal.capture());
        assertEquals("UTC", cal.getValue().getTimeZone().getID());
    }

    @Test
    void readDateTime_returnsNullForSqlNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp(eq("lastUpdate"), any(Calendar.class))).thenReturn(null);

        assertNull(GeneralDaoImpl.readDateTime(rs, "lastUpdate"));
    }

    @Test
    void bindDateTime_setsTimestampWithUtcCalendar() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        GeneralDaoImpl.bindDateTime(ps, 4, CREATED);

        ArgumentCaptor<Calendar> cal = ArgumentCaptor.forClass(Calendar.class);
        verify(ps).setTimestamp(eq(4), eq(asDbTimestamp(CREATED)), cal.capture());
        assertEquals("UTC", cal.getValue().getTimeZone().getID());
    }
}
