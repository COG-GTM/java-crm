package Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import DAO.AppointmentDaoImpl;
import Model.Appointment;
import Model.Customer;
import Model.User;
import Utilities.RBMain;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

/**
 * Unit tests for {@link UpdateAppointmentScreenController}'s private
 * {@code submit(Event)} validation logic.
 *
 * <p>Mirrors {@link AddAppointmentScreenControllerTest}: the same ordered guard
 * clauses (required field, date, customer, start-before-end, business-hours
 * {@code assert}s) plus the overlap rejection - here exercised in <em>update</em>
 * mode ({@code checkAppointmentOverlap(..., true)}). Controls are injected via
 * reflection and {@link AppointmentDaoImpl} is stubbed with
 * {@code Mockito.mockStatic}.</p>
 */
class UpdateAppointmentScreenControllerTest extends JavaFxTestBase {

    private static final LocalDate DATE = LocalDate.of(2021, 5, 3);

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
        Customer.setCurrentCustomer(null);
        Appointment.setCurrentAppointment(null);
    }

    private static UpdateAppointmentScreenController newController() {
        UpdateAppointmentScreenController controller = new UpdateAppointmentScreenController();
        setField(controller, "typeTxt", new TextField("Consult"));
        setField(controller, "titleTxt", new TextField("Kickoff"));
        setField(controller, "descriptionTxt", new TextField("desc"));
        setField(controller, "locationTxt", new TextField("HQ"));
        setField(controller, "contactTxt", new TextField("Pat"));
        setField(controller, "urlTxt", new TextField("http://x"));
        setField(controller, "datePicker", new DatePicker(DATE));
        setField(controller, "startTimeCombo", comboOf("8:00 AM"));
        setField(controller, "endTimeCombo", comboOf("9:00 AM"));
        ComboBox<Customer> customerCombo = new ComboBox<>();
        customerCombo.setValue(Fixtures.customer(9, "Acme"));
        setField(controller, "customerComboBox", customerCombo);
        setField(controller, "errorLbl", new Label());
        return controller;
    }

    private static ComboBox<String> comboOf(String value) {
        ComboBox<String> combo = new ComboBox<>();
        combo.setValue(value);
        return combo;
    }

    private static void submit(UpdateAppointmentScreenController controller) {
        invokePrivate(controller, "submit",
                new Class<?>[]{Event.class}, new Object[]{new ActionEvent()});
    }

    private static Throwable submitCatching(UpdateAppointmentScreenController controller) {
        try {
            Method m = UpdateAppointmentScreenController.class
                    .getDeclaredMethod("submit", Event.class);
            m.setAccessible(true);
            m.invoke(controller, new ActionEvent());
            return null;
        } catch (InvocationTargetException e) {
            return e.getCause();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String errorText(UpdateAppointmentScreenController controller) {
        return ((Label) getFieldValue(controller, "errorLbl")).getText();
    }

    @Test
    void blankRequiredFieldShowsEmptyFieldMessageAndUpdatesNothing() throws Exception {
        RBMain.setRb();
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            setField(controller, "typeTxt", new TextField(""));

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("One or more fields are empty", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void missingDateShowsSelectADate() throws Exception {
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            setField(controller, "datePicker", new DatePicker());

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("Select a date", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void missingCustomerShowsSelectACustomer() throws Exception {
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            setField(controller, "customerComboBox", new ComboBox<Customer>());

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("Select a customer", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void startNotBeforeEndShowsOrderingMessage() throws Exception {
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            setField(controller, "startTimeCombo", comboOf("10:00 AM"));
            setField(controller, "endTimeCombo", comboOf("9:00 AM")); // start after end

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("Start time must be before end time", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void endAfterBusinessHoursTripsBusinessHoursAssertion() throws Exception {
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            setField(controller, "startTimeCombo", comboOf("8:00 AM"));
            setField(controller, "endTimeCombo", comboOf("6:00 PM"));

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                Throwable thrown = submitCatching(controller);

                assertNotNull(thrown, "expected the business-hours assertion to fail");
                assertInstanceOf(AssertionError.class, thrown);
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void overlappingAppointmentIsRejectedWithBusinessMessage() throws Exception {
        runOnFxThread(() -> {
            UpdateAppointmentScreenController controller = newController();
            User.setCurrentUser(Fixtures.user(3, "admin"));
            Appointment.setCurrentAppointment(Fixtures.appointment(7, 9, 3));
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(8, 0));
            LocalDateTime end = LocalDateTime.of(DATE, LocalTime.of(9, 0));

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                apptMock.when(() -> AppointmentDaoImpl.checkAppointmentOverlap(
                        eq(3), eq(9), eq(start), eq(end), eq(true))).thenReturn(true);

                submit(controller);

                assertEquals("Appointment time overlaps with existing appointment",
                        errorText(controller));
                apptMock.verify(() -> AppointmentDaoImpl.updateAppointment(anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        eq(start), eq(end), anyInt(), anyInt()), never());
            }
        });
    }

    private static Object getFieldValue(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read field '" + fieldName + "'", e);
        }
    }
}
