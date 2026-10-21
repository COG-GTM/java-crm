package DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.Appointment;
import Model.Customer;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

/**
 * Verifies that AppointmentDaoImpl exchanges DATETIME values with the driver as
 * java.sql.Timestamp (anchored to UTC) instead of hand-formatted strings.
 */
class AppointmentDaoImplTimestampTest {

    private static final LocalDateTime START = LocalDateTime.of(2021, 3, 14, 9, 30);
    private static final LocalDateTime END = START.plusHours(1);

    private MockedStatic<DBQuery> dbQuery;
    private PreparedStatement ps;
    private ResultSet rs;

    @BeforeEach
    void setUp() throws Exception {
        LocalDateTime audit = LocalDateTime.of(2021, 1, 1, 0, 0);
        User.setCurrentUser(new User(3, "admin", "", true, audit, "test", audit, "test"));
        Customer.setCurrentCustomer(new Customer(9, "Acme", 1, true, audit, "test", audit, "test"));

        dbQuery = mockStatic(DBQuery.class);
        ps = mock(PreparedStatement.class);
        rs = mock(ResultSet.class);
        dbQuery.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
        when(ps.getResultSet()).thenReturn(rs);
    }

    @AfterEach
    void tearDown() {
        dbQuery.close();
        User.setCurrentUser(null);
        Customer.setCurrentCustomer(null);
        Appointment.setCurrentAppointment(null);
    }

    // The instant a user-zone wall-clock value represents, as the driver hands it back.
    private static Timestamp asDbTimestamp(LocalDateTime userLocal) {
        return Timestamp.from(userLocal.atZone(ZoneId.systemDefault()).toInstant());
    }

    private void stubRow() throws Exception {
        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("appointmentId")).thenReturn(15);
        when(rs.getInt("customerId")).thenReturn(9);
        when(rs.getString("title")).thenReturn("Kickoff");
        when(rs.getString("description")).thenReturn("desc");
        when(rs.getString("location")).thenReturn("HQ");
        when(rs.getString("contact")).thenReturn("Pat");
        when(rs.getString("type")).thenReturn("Consult");
        when(rs.getString("url")).thenReturn("http://x");
        when(rs.getString("createdBy")).thenReturn("test");
        when(rs.getString("lastUpdateBy")).thenReturn("test");

        // MySQL Connector/J 8 renders DATETIME(0) columns without a fractional part,
        // which the legacy "yyyy-MM-dd HH:mm:ss.S" parser cannot read.
        when(rs.getString("start")).thenReturn("2021-03-14 09:30:00");
        when(rs.getString("end")).thenReturn("2021-03-14 10:30:00");
        when(rs.getString("createDate")).thenReturn("2021-01-01 00:00:00");
        when(rs.getString("lastUpdate")).thenReturn("2021-01-01 00:00:00");

        when(rs.getTimestamp(eq("start"), any(Calendar.class))).thenReturn(asDbTimestamp(START));
        when(rs.getTimestamp(eq("end"), any(Calendar.class))).thenReturn(asDbTimestamp(END));
        when(rs.getTimestamp(eq("createDate"), any(Calendar.class))).thenReturn(asDbTimestamp(START));
        when(rs.getTimestamp(eq("lastUpdate"), any(Calendar.class))).thenReturn(asDbTimestamp(START));
    }

    @Test
    void getAppointmentList_readsDateTimeColumnsAsTimestamps() throws Exception {
        stubRow();

        ObservableList<Appointment> list = AppointmentDaoImpl.getAppointmentList(3, START, START);

        assertEquals(1, list.size());
        assertEquals(START, list.get(0).getStart());
        assertEquals(END, list.get(0).getEnd());
        assertEquals(START, list.get(0).getCreateDate());
        verify(rs, never()).getString("start");
        verify(rs, never()).getString("end");
    }

    @Test
    void getAppointmentList_bindsSearchWindowAsUtcTimestamps() throws Exception {
        when(rs.next()).thenReturn(false);

        AppointmentDaoImpl.getAppointmentList(3, START, END);

        ArgumentCaptor<Calendar> cal = ArgumentCaptor.forClass(Calendar.class);
        verify(ps).setTimestamp(eq(2), eq(asDbTimestamp(START)), cal.capture());
        verify(ps).setTimestamp(eq(3), eq(asDbTimestamp(END)), cal.capture());
        assertTrue(cal.getAllValues().stream().allMatch(c -> "UTC".equals(c.getTimeZone().getID())));
        verify(ps, never()).setString(anyInt(), anyString());
    }

    @Test
    void insertAppointment_bindsStartEndAndAuditColumnsAsTimestamps() throws Exception {
        when(rs.next()).thenReturn(false);

        AppointmentDaoImpl.insertAppointment("t", "d", "l", "c", "type", "u", START, END);

        verify(ps).setTimestamp(eq(9), eq(asDbTimestamp(START)), any(Calendar.class));
        verify(ps).setTimestamp(eq(10), eq(asDbTimestamp(END)), any(Calendar.class));
        verify(ps).setTimestamp(eq(11), any(Timestamp.class), any(Calendar.class));
        verify(ps).setTimestamp(eq(13), any(Timestamp.class), any(Calendar.class));
        verify(ps, never()).setString(eq(9), anyString());
        verify(ps, never()).setString(eq(10), anyString());
        verify(ps, never()).setString(eq(11), anyString());
        verify(ps, never()).setString(eq(13), anyString());
    }

    @Test
    void checkAppointmentOverlap_bindsInclusiveWindowAsTimestamps() throws Exception {
        when(rs.next()).thenReturn(false);

        AppointmentDaoImpl.checkAppointmentOverlap(3, 9, START, END, false);

        verify(ps).setTimestamp(eq(3), eq(asDbTimestamp(START)), any(Calendar.class));
        verify(ps).setTimestamp(eq(4), eq(asDbTimestamp(END.minusSeconds(1))), any(Calendar.class));
        verify(ps).setTimestamp(eq(5), eq(asDbTimestamp(START.plusSeconds(1))), any(Calendar.class));
        verify(ps).setTimestamp(eq(6), eq(asDbTimestamp(END)), any(Calendar.class));
    }
}
