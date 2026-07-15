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
import Model.Customer;
import Model.User;
import Utilities.RBMain;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Unit tests for {@link AddAppointmentScreenController}'s private
 * {@code submit(Event)} validation logic.
 *
 * <p>The controller guards a new appointment behind, in order: a required-field
 * check ({@code typeTxt}), a date-selected check, a customer-selected check, a
 * start-before-end check and the business-hours (8am-5pm) {@code assert}s. Real
 * JavaFX controls are injected into the private {@code @FXML} fields via
 * reflection (see {@link JavaFxTestBase}) and {@link AppointmentDaoImpl} is
 * stubbed with {@code Mockito.mockStatic} so no live database is required.</p>
 */
class AddAppointmentScreenControllerTest extends JavaFxTestBase {

    private static final LocalDate DATE = LocalDate.of(2021, 5, 3);

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
        Customer.setCurrentCustomer(null);
    }

    private static AddAppointmentScreenController newController() {
        AddAppointmentScreenController controller = new AddAppointmentScreenController();
        // Text fields read via getText() on the happy path - populate them all.
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

    private static void submit(AddAppointmentScreenController controller) {
        invokePrivate(controller, "submit",
                new Class<?>[]{Event.class}, new Object[]{new ActionEvent()});
    }

    private static Throwable submitCatching(AddAppointmentScreenController controller) {
        try {
            Method m = AddAppointmentScreenController.class
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

    private static String errorText(AddAppointmentScreenController controller) {
        return ((Label) getFieldValue(controller, "errorLbl")).getText();
    }

    @Test
    void blankRequiredFieldShowsEmptyFieldMessageAndInsertsNothing() throws Exception {
        RBMain.setRb();
        runOnFxThread(() -> {
            AddAppointmentScreenController controller = newController();
            setField(controller, "typeTxt", new TextField("")); // empty required field

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("One or more fields are empty", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void missingDateShowsSelectADateAndInsertsNothing() throws Exception {
        runOnFxThread(() -> {
            AddAppointmentScreenController controller = newController();
            setField(controller, "datePicker", new DatePicker()); // no date

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("Select a date", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void missingCustomerShowsSelectACustomerAndInsertsNothing() throws Exception {
        runOnFxThread(() -> {
            AddAppointmentScreenController controller = newController();
            setField(controller, "customerComboBox", new ComboBox<Customer>()); // no customer

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                submit(controller);

                assertEquals("Select a customer", errorText(controller));
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void startNotBeforeEndShowsOrderingMessageAndInsertsNothing() throws Exception {
        runOnFxThread(() -> {
            AddAppointmentScreenController controller = newController();
            setField(controller, "startTimeCombo", comboOf("9:00 AM"));
            setField(controller, "endTimeCombo", comboOf("9:00 AM")); // equal -> not before

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
            AddAppointmentScreenController controller = newController();
            User.setCurrentUser(Fixtures.user());
            setField(controller, "startTimeCombo", comboOf("8:00 AM"));
            setField(controller, "endTimeCombo", comboOf("6:00 PM")); // 18:00 > 17:00

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                Throwable thrown = submitCatching(controller);

                assertNotNull(thrown, "expected the business-hours assertion to fail");
                assertInstanceOf(AssertionError.class, thrown);
                // The assert fires before any DB work.
                apptMock.verifyNoInteractions();
            }
        });
    }

    @Test
    void overlappingAppointmentIsRejectedWithBusinessMessage() throws Exception {
        runOnFxThread(() -> {
            AddAppointmentScreenController controller = newController();
            User.setCurrentUser(Fixtures.user(3, "admin"));
            LocalDateTime start = LocalDateTime.of(DATE, java.time.LocalTime.of(8, 0));
            LocalDateTime end = LocalDateTime.of(DATE, java.time.LocalTime.of(9, 0));

            try (MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {
                apptMock.when(() -> AppointmentDaoImpl.checkAppointmentOverlap(
                        eq(3), eq(9), eq(start), eq(end), eq(false))).thenReturn(true);

                submit(controller);

                assertEquals("Appointment time overlaps with existing appointment",
                        errorText(controller));
                // Overlap short-circuits: the insert never runs.
                apptMock.verify(() -> AppointmentDaoImpl.insertAppointment(anyString(), anyString(),
                        anyString(), anyString(), anyString(), anyString(),
                        eq(start), eq(end)), never());
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
