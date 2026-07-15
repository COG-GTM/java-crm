package Model;

import DAO.CustomerDaoImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import testsupport.Fixtures;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class AppointmentTest {

    private Appointment build() {
        LocalDateTime start = LocalDateTime.of(2021, 3, 15, 9, 5);
        LocalDateTime end = LocalDateTime.of(2021, 3, 15, 14, 30);
        LocalDateTime created = LocalDateTime.of(2021, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2021, 1, 2, 0, 0);
        return new Appointment(11, 22, 33, "Title", "Desc", "Loc", "Contact",
                "Type", "http://url", start, end, created, "creator", updated, "editor");
    }

    @BeforeEach
    @AfterEach
    void resetStatic() {
        Appointment.setCurrentAppointment(null);
    }

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime start = LocalDateTime.of(2021, 3, 15, 9, 5);
        LocalDateTime end = LocalDateTime.of(2021, 3, 15, 14, 30);
        LocalDateTime created = LocalDateTime.of(2021, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2021, 1, 2, 0, 0);
        Appointment appt = new Appointment(11, 22, 33, "Title", "Desc", "Loc", "Contact",
                "Type", "http://url", start, end, created, "creator", updated, "editor");

        assertEquals(11, appt.getAppointmentId());
        assertEquals(22, appt.getCustomerId());
        assertEquals(33, appt.getUserId());
        assertEquals("Title", appt.getTitle());
        assertEquals("Desc", appt.getDescription());
        assertEquals("Loc", appt.getLocation());
        assertEquals("Contact", appt.getContact());
        assertEquals("Type", appt.getType());
        assertEquals("http://url", appt.getUrl());
        assertEquals(start, appt.getStart());
        assertEquals(end, appt.getEnd());
        assertEquals(created, appt.getCreateDate());
        assertEquals("creator", appt.getCreatedBy());
        assertEquals(updated, appt.getLastUpdate());
        assertEquals("editor", appt.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        Appointment appt = Fixtures.appointment();
        LocalDateTime start = LocalDateTime.of(2022, 4, 4, 8, 0);
        LocalDateTime end = LocalDateTime.of(2022, 4, 4, 9, 0);
        LocalDateTime created = LocalDateTime.of(2022, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2022, 2, 2, 0, 0);

        appt.setAppointmentId(101);
        appt.setCustomerId(202);
        appt.setUserId(303);
        appt.setTitle("t2");
        appt.setDescription("d2");
        appt.setLocation("l2");
        appt.setContact("con2");
        appt.setType("ty2");
        appt.setUrl("u2");
        appt.setStart(start);
        appt.setEnd(end);
        appt.setCreateDate(created);
        appt.setCreatedBy("cb2");
        appt.setLastUpdate(updated);
        appt.setLastUpdateBy("ub2");

        assertEquals(101, appt.getAppointmentId());
        assertEquals(202, appt.getCustomerId());
        assertEquals(303, appt.getUserId());
        assertEquals("t2", appt.getTitle());
        assertEquals("d2", appt.getDescription());
        assertEquals("l2", appt.getLocation());
        assertEquals("con2", appt.getContact());
        assertEquals("ty2", appt.getType());
        assertEquals("u2", appt.getUrl());
        assertEquals(start, appt.getStart());
        assertEquals(end, appt.getEnd());
        assertEquals(created, appt.getCreateDate());
        assertEquals("cb2", appt.getCreatedBy());
        assertEquals(updated, appt.getLastUpdate());
        assertEquals("ub2", appt.getLastUpdateBy());
    }

    @Test
    void currentAppointment_roundTrips() {
        assertNull(Appointment.getCurrentAppointment());

        Appointment appt = Fixtures.appointment();
        Appointment.setCurrentAppointment(appt);
        assertSame(appt, Appointment.getCurrentAppointment());

        Appointment.setCurrentAppointment(null);
        assertNull(Appointment.getCurrentAppointment());
    }

    @Test
    void getDate_formatsStartAsUiDate() {
        assertEquals("3/15/2021", build().getDate());
    }

    @Test
    void getStartTime_formatsStartAsUiTime() {
        assertEquals("9:05 AM", build().getStartTime());
    }

    @Test
    void getEndTime_formatsEndAsUiTime() {
        assertEquals("2:30 PM", build().getEndTime());
    }

    @Test
    void getCustomerName_delegatesToCustomerDao() throws Exception {
        Appointment appt = build();
        try (MockedStatic<CustomerDaoImpl> mocked = Mockito.mockStatic(CustomerDaoImpl.class)) {
            mocked.when(() -> CustomerDaoImpl.getCustomer(22))
                    .thenReturn(Fixtures.customer(22, "Wayne Enterprises"));

            assertEquals("Wayne Enterprises", appt.getCustomerName());
            mocked.verify(() -> CustomerDaoImpl.getCustomer(22));
        }
    }
}
