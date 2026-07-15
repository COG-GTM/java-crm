package DAO;

import static DAO.DaoTestSupport.DB_TS;
import static DAO.DaoTestSupport.stubAuditColumns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.Appointment;
import Model.Customer;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

class AppointmentDaoImplTest {

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
        Customer.setCurrentCustomer(null);
        Appointment.setCurrentAppointment(null);
    }

    @Test
    void getAppointment_bindsKeys_andMapsRow() throws Exception {
        User.setCurrentUser(Fixtures.user(3, "admin"));
        Customer.setCurrentCustomer(Fixtures.customer(9, "Acme"));
        LocalDateTime start = LocalDateTime.of(2021, 5, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("appointmentId")).thenReturn(15);
            when(rs.getString("title")).thenReturn("Kickoff");
            when(rs.getString("description")).thenReturn("desc");
            when(rs.getString("location")).thenReturn("HQ");
            when(rs.getString("contact")).thenReturn("Pat");
            when(rs.getString("type")).thenReturn("Consult");
            when(rs.getString("url")).thenReturn("http://x");
            stubAuditColumns(rs);

            Appointment appt = AppointmentDaoImpl.getAppointment(start, end);

            assertEquals(15, appt.getAppointmentId());
            assertEquals("Kickoff", appt.getTitle());
            assertEquals(9, appt.getCustomerId());
            assertEquals(3, appt.getUserId());
            assertEquals(start, appt.getStart());
            verify(ps).setInt(1, 9);
            verify(ps).setInt(2, 3);
        }
    }

    @Test
    void getAppointmentList_allForUser_mapsRows() throws Exception {
        // Equal start/end => "all appointments for user" branch (no date binding).
        LocalDateTime same = LocalDateTime.of(2021, 1, 1, 0, 0);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("appointmentId")).thenReturn(15);
            when(rs.getInt("customerId")).thenReturn(9);
            when(rs.getString("title")).thenReturn("Kickoff");
            when(rs.getString("description")).thenReturn("desc");
            when(rs.getString("location")).thenReturn("HQ");
            when(rs.getString("contact")).thenReturn("Pat");
            when(rs.getString("type")).thenReturn("Consult");
            when(rs.getString("url")).thenReturn("http://x");
            when(rs.getString("start")).thenReturn(DB_TS);
            when(rs.getString("end")).thenReturn(DB_TS);
            stubAuditColumns(rs);

            ObservableList<Appointment> list = AppointmentDaoImpl.getAppointmentList(3, same, same);

            assertEquals(1, list.size());
            assertEquals("Kickoff", list.get(0).getTitle());
            assertEquals(9, list.get(0).getCustomerId());
            assertEquals(3, list.get(0).getUserId());
            verify(ps).setInt(1, 3);
        }
    }

    @Test
    void getAppointmentList_returnsEmpty_whenNoRows() throws Exception {
        LocalDateTime same = LocalDateTime.of(2021, 1, 1, 0, 0);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertTrue(AppointmentDaoImpl.getAppointmentList(3, same, same).isEmpty());
        }
    }

    @Test
    void deleteAppointment_bindsId_andReturnsUpdateCount() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getUpdateCount()).thenReturn(1);

            int rows = AppointmentDaoImpl.deleteAppointment(12);

            assertEquals(1, rows);
            verify(ps).setInt(1, 12);
            verify(ps).execute();
        }
    }

    @Test
    void checkAppointmentOverlap_returnsTrue_whenRowExists_addMode() throws Exception {
        LocalDateTime start = LocalDateTime.of(2021, 5, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            boolean overlap = AppointmentDaoImpl.checkAppointmentOverlap(3, 9, start, end, false);

            assertTrue(overlap);
            verify(ps).setInt(1, 3);
            verify(ps).setInt(2, 9);
        }
    }

    @Test
    void checkAppointmentOverlap_returnsFalse_whenNoRows_addMode() throws Exception {
        LocalDateTime start = LocalDateTime.of(2021, 5, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertFalse(AppointmentDaoImpl.checkAppointmentOverlap(3, 9, start, end, false));
        }
    }

    @Test
    void checkAppointmentOverlap_ignoresCurrentAppointment_inUpdateMode() throws Exception {
        // The only overlapping row is the appointment being edited => not a conflict.
        Appointment.setCurrentAppointment(Fixtures.appointment(1, 9, 3));
        LocalDateTime start = LocalDateTime.of(2021, 5, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("appointmentId")).thenReturn(1);

            assertFalse(AppointmentDaoImpl.checkAppointmentOverlap(3, 9, start, end, true));
        }
    }

    @Test
    void checkAppointmentOverlap_detectsOtherAppointment_inUpdateMode() throws Exception {
        Appointment.setCurrentAppointment(Fixtures.appointment(1, 9, 3));
        LocalDateTime start = LocalDateTime.of(2021, 5, 1, 9, 0);
        LocalDateTime end = start.plusHours(1);
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("appointmentId")).thenReturn(5);

            assertTrue(AppointmentDaoImpl.checkAppointmentOverlap(3, 9, start, end, true));
        }
    }

    @Test
    void getTypeReport_forUser_bindsParams_andAssemblesReport() throws Exception {
        ObservableList<String> searchTimes =
                FXCollections.observableArrayList("2021-01-01 00:00:00.0", "2021-12-31 23:59:59.0");
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getString("apptperiod")).thenReturn("January 2021", "January 2021");
            when(rs.getString("type")).thenReturn("Consult", "Meeting");
            when(rs.getInt("appointments")).thenReturn(2, 1);

            String report = AppointmentDaoImpl.getTypeReport(searchTimes, 3);

            assertEquals("   January 2021\nConsult: 2\nMeeting: 1\n", report);
            verify(ps).setInt(1, 3);
            verify(ps).setString(2, "2021-01-01 00:00:00.0");
            verify(ps).setString(3, "2021-12-31 23:59:59.0");
        }
    }

    @Test
    void getAvgApptReport_bindsParams_andAssemblesReport() throws Exception {
        ObservableList<String> searchTimes =
                FXCollections.observableArrayList("2021-01-01 00:00:00.0", "2021-12-31 23:59:59.0");
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("apptperiod")).thenReturn("January 2021");
            when(rs.getDouble("appointments")).thenReturn(3.5);

            String report = AppointmentDaoImpl.getAvgApptReport(searchTimes);

            assertEquals("January 2021: 3.5\n", report);
            verify(ps).setString(1, "2021-01-01 00:00:00.0");
            verify(ps).setString(2, "2021-12-31 23:59:59.0");
        }
    }

    @Test
    void checkUpcomingAppt_returnsEmptyString_whenNoUpcoming() throws Exception {
        User.setCurrentUser(Fixtures.user(3, "admin"));
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertEquals("", AppointmentDaoImpl.checkUpcomingAppt());
            verify(ps).setInt(1, 3);
        }
    }
}
