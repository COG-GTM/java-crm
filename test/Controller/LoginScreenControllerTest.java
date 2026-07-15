package Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import DAO.AppointmentDaoImpl;
import DAO.UserDaoImpl;
import Model.User;
import Utilities.LogFiles;
import Utilities.RBMain;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

/**
 * Unit tests for {@link LoginScreenController}'s private {@code login(Event)}
 * branch logic.
 *
 * <p>Real {@code TextField}/{@code PasswordField}/{@code Label} controls are
 * injected into the private {@code @FXML} fields and the static collaborators
 * ({@code UserDaoImpl.logIn}, {@code LogFiles.logUserActivity},
 * {@code AppointmentDaoImpl.checkUpcomingAppt}) are stubbed with
 * {@code Mockito.mockStatic(...)}.</p>
 *
 * <p>Coverage:</p>
 * <ul>
 *   <li>Empty fields -> {@code checkFields} short-circuits, the error label is
 *       populated and {@code UserDaoImpl.logIn} is never consulted.</li>
 *   <li>Successful credentials -> {@code logIn}, {@code logUserActivity} and
 *       {@code checkUpcomingAppt} are invoked before screen navigation.</li>
 * </ul>
 *
 * <p>Not unit-tested (requires a live UI): after a successful login the controller
 * calls {@code displayScreen(...)} (FXML/Stage) to reach the main menu; the
 * failed-credentials branch throws {@code BusinessException} and terminates in
 * {@code displayErrorAlert(...)} which shows a blocking modal alert
 * ({@code Alert.showAndWait()}) requiring user interaction.</p>
 */
class LoginScreenControllerTest extends JavaFxTestBase {

    private static final String USERNAME = "austin";
    private static final String PASSWORD = "s3cret";

    @BeforeAll
    static void initResourceBundle() {
        // GeneralController.checkFields / login read messages from the resource bundle.
        RBMain.setRb();
    }

    @AfterEach
    void clearCurrentUser() {
        User.setCurrentUser(null);
    }

    private static LoginScreenController newController(TextField userNameTxt, PasswordField passwordTxt) {
        LoginScreenController controller = new LoginScreenController();
        setField(controller, "userNameTxt", userNameTxt);
        setField(controller, "passwordTxt", passwordTxt);
        setField(controller, "errorLbl", new Label());
        return controller;
    }

    private static void invokeLogin(LoginScreenController controller, Event event) {
        invokePrivate(controller, "login",
                new Class<?>[]{Event.class}, new Object[]{event});
    }

    @Test
    void loginWithEmptyFieldsShowsErrorAndDoesNotHitDao() throws Exception {
        runOnFxThread(() -> {
            LoginScreenController controller = newController(new TextField(), new PasswordField());

            try (MockedStatic<UserDaoImpl> userMock = mockStatic(UserDaoImpl.class)) {
                invokeLogin(controller, new ActionEvent());

                userMock.verify(() -> UserDaoImpl.logIn(anyString(), anyString()), never());

                Label errorLbl = (Label) getFieldValue(controller, "errorLbl");
                assertEquals("One or more fields are empty", errorLbl.getText());
            }
        });
    }

    @Test
    void loginWithValidCredentialsInvokesActivityLogAndUpcomingApptBeforeNavigation() throws Exception {
        runOnFxThread(() -> {
            LoginScreenController controller =
                    newController(new TextField(USERNAME), new PasswordField());
            ((PasswordField) getFieldValue(controller, "passwordTxt")).setText(PASSWORD);

            try (MockedStatic<UserDaoImpl> userMock = mockStatic(UserDaoImpl.class);
                 MockedStatic<LogFiles> logMock = mockStatic(LogFiles.class);
                 MockedStatic<AppointmentDaoImpl> apptMock = mockStatic(AppointmentDaoImpl.class)) {

                userMock.when(() -> UserDaoImpl.logIn(USERNAME, PASSWORD)).thenReturn(true);
                apptMock.when(AppointmentDaoImpl::checkUpcomingAppt).thenReturn("");

                // After the mocked collaborators run, displayScreen navigates to the main
                // menu (FXML/Stage) which cannot run headlessly and throws.
                assertThrows(RuntimeException.class, () -> invokeLogin(controller, new ActionEvent()));

                userMock.verify(() -> UserDaoImpl.logIn(USERNAME, PASSWORD));
                logMock.verify(LogFiles::logUserActivity);
                apptMock.verify(AppointmentDaoImpl::checkUpcomingAppt);
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
