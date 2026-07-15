package Controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import Model.User;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

/**
 * Unit tests for {@link CalendarScreenController}'s non-UI ownership check.
 *
 * <p>Exercises the private {@code currentUserSelected()} logic, which compares
 * the user selected in {@code userComboBox} against {@code User.getCurrentUser()}.
 * A real {@code ComboBox<User>} is injected into the private {@code @FXML} field
 * via reflection and {@code User.setCurrentUser(...)} is set to drive the two
 * branches. The FXML/Stage navigation paths (add/update/delete handlers) are out
 * of scope as they require a live UI.</p>
 */
class CalendarScreenControllerTest extends JavaFxTestBase {

    @AfterEach
    void clearCurrentUser() {
        User.setCurrentUser(null);
    }

    @Test
    void currentUserSelectedReturnsTrueWhenSelectedUserMatchesCurrentUser() throws Exception {
        CalendarScreenController controller = new CalendarScreenController();

        runOnFxThread(() -> {
            User user = Fixtures.user(7, "alice");
            User.setCurrentUser(Fixtures.user(7, "alice"));

            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setValue(user);
            setField(controller, "userComboBox", userComboBox);

            boolean result = (boolean) invokePrivate(controller, "currentUserSelected");
            assertTrue(result, "Selecting the current user's id should be recognized as the current user");
        });
    }

    @Test
    void currentUserSelectedReturnsFalseWhenSelectedUserDiffersFromCurrentUser() throws Exception {
        CalendarScreenController controller = new CalendarScreenController();

        runOnFxThread(() -> {
            User.setCurrentUser(Fixtures.user(7, "alice"));

            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setValue(Fixtures.user(9, "bob"));
            setField(controller, "userComboBox", userComboBox);

            boolean result = (boolean) invokePrivate(controller, "currentUserSelected");
            assertFalse(result, "Selecting a different user's id should not be recognized as the current user");
        });
    }
}
