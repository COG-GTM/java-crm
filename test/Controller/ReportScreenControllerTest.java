package Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import DAO.AppointmentDaoImpl;
import Model.Report;
import Model.User;
import Utilities.TimeFiles;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

/**
 * Unit tests for {@link ReportScreenController}'s private report-dispatch
 * {@code switch} in {@code onActionGenerateReport}.
 *
 * <p>For each {@code reportId} 1..6 the controller reads the selected
 * {@code Report}, obtains rolling/calendar-year search windows from
 * {@code Utilities.TimeFiles.getSearchTimes(...)} and calls the matching static
 * on {@code DAO.AppointmentDaoImpl}, writing the result into {@code reportTextArea}.
 * Both static collaborators are stubbed with {@code Mockito.mockStatic(...)} so
 * the exact method/args can be verified and the returned text asserted.</p>
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>{@code ReportScreenController} captures {@code User.getCurrentUser().getUserId()}
 *       in a field initializer, so a current user is set before construction.</li>
 *   <li>Mockito static mocks are thread-local, so they are opened <em>inside</em>
 *       the JavaFX Application Thread action that invokes the controller.</li>
 * </ul>
 */
class ReportScreenControllerTest extends JavaFxTestBase {

    private static final int CURRENT_USER_ID = 42;
    private static final String STUB_RESULT = "REPORT-TEXT";

    @BeforeEach
    void setCurrentUser() {
        User.setCurrentUser(Fixtures.user(CURRENT_USER_ID, "reporter"));
    }

    @AfterEach
    void clearCurrentUser() {
        User.setCurrentUser(null);
    }

    @Test
    void reportId1UsesRollingYearForCurrentUser() throws Exception {
        assertTypeReportDispatch(1, true, CURRENT_USER_ID);
    }

    @Test
    void reportId2UsesCalendarYearForCurrentUser() throws Exception {
        assertTypeReportDispatch(2, false, CURRENT_USER_ID);
    }

    @Test
    void reportId3UsesRollingYearForAllUsers() throws Exception {
        assertTypeReportDispatch(3, true, null);
    }

    @Test
    void reportId4UsesCalendarYearForAllUsers() throws Exception {
        assertTypeReportDispatch(4, false, null);
    }

    @Test
    void reportId5UsesAvgReportRollingYear() throws Exception {
        assertAvgReportDispatch(5, true);
    }

    @Test
    void reportId6UsesAvgReportCalendarYear() throws Exception {
        assertAvgReportDispatch(6, false);
    }

    private void assertTypeReportDispatch(int reportId, boolean rolling, Integer expectedUserArg)
            throws Exception {
        ReportScreenController controller = new ReportScreenController();
        final ObservableList<String> rollingYear = FXCollections.observableArrayList("rolling");
        final ObservableList<String> calendarYear = FXCollections.observableArrayList("calendar");
        final ObservableList<String> expectedTimes = rolling ? rollingYear : calendarYear;

        runOnFxThread(() -> {
            TextArea reportTextArea = new TextArea();
            setField(controller, "reportTextArea", reportTextArea);
            setField(controller, "reportComboBox", comboWithSelectedReport(reportId));

            try (MockedStatic<TimeFiles> timeMock = mockStatic(TimeFiles.class);
                 MockedStatic<AppointmentDaoImpl> daoMock = mockStatic(AppointmentDaoImpl.class)) {

                timeMock.when(() -> TimeFiles.getSearchTimes(any(LocalDateTime.class), eq(true)))
                        .thenReturn(rollingYear);
                timeMock.when(() -> TimeFiles.getSearchTimes(any(LocalDateTime.class), eq(false)))
                        .thenReturn(calendarYear);
                daoMock.when(() -> AppointmentDaoImpl.getTypeReport(expectedTimes, expectedUserArg))
                        .thenReturn(STUB_RESULT);

                invokePrivate(controller, "onActionGenerateReport",
                        new Class<?>[]{ActionEvent.class}, new Object[]{new ActionEvent()});

                assertEquals(STUB_RESULT, reportTextArea.getText(),
                        "reportTextArea should contain the DAO result for reportId " + reportId);
                daoMock.verify(() -> AppointmentDaoImpl.getTypeReport(expectedTimes, expectedUserArg));
            }
        });
    }

    private void assertAvgReportDispatch(int reportId, boolean rolling) throws Exception {
        ReportScreenController controller = new ReportScreenController();
        final ObservableList<String> rollingYear = FXCollections.observableArrayList("rolling");
        final ObservableList<String> calendarYear = FXCollections.observableArrayList("calendar");
        final ObservableList<String> expectedTimes = rolling ? rollingYear : calendarYear;

        runOnFxThread(() -> {
            TextArea reportTextArea = new TextArea();
            setField(controller, "reportTextArea", reportTextArea);
            setField(controller, "reportComboBox", comboWithSelectedReport(reportId));

            try (MockedStatic<TimeFiles> timeMock = mockStatic(TimeFiles.class);
                 MockedStatic<AppointmentDaoImpl> daoMock = mockStatic(AppointmentDaoImpl.class)) {

                timeMock.when(() -> TimeFiles.getSearchTimes(any(LocalDateTime.class), eq(true)))
                        .thenReturn(rollingYear);
                timeMock.when(() -> TimeFiles.getSearchTimes(any(LocalDateTime.class), eq(false)))
                        .thenReturn(calendarYear);
                daoMock.when(() -> AppointmentDaoImpl.getAvgApptReport(expectedTimes))
                        .thenReturn(STUB_RESULT);

                invokePrivate(controller, "onActionGenerateReport",
                        new Class<?>[]{ActionEvent.class}, new Object[]{new ActionEvent()});

                assertEquals(STUB_RESULT, reportTextArea.getText(),
                        "reportTextArea should contain the DAO result for reportId " + reportId);
                daoMock.verify(() -> AppointmentDaoImpl.getAvgApptReport(expectedTimes));
            }
        });
    }

    private static ComboBox<Report> comboWithSelectedReport(int reportId) {
        ComboBox<Report> reportComboBox = new ComboBox<>();
        reportComboBox.setValue(Fixtures.report("Report " + reportId, reportId));
        return reportComboBox;
    }
}
