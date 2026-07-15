package Controller;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import Model.User;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

/**
 * Unit tests for {@link MainMenuController}.
 *
 * <p>The navigation handlers all delegate to {@code displayScreen(...)} which
 * loads FXML and manipulates a {@code Stage} (out of scope for unit tests). The
 * one piece of non-UI logic is {@code onActionLogout}, which resets the current
 * user via {@code User.setCurrentUser(null)} <em>before</em> navigating. This test
 * asserts that reset: the subsequent {@code displayScreen} call fails fast (it
 * casts {@code event.getSource()} to a JavaFX {@code Control}), but the current
 * user has already been cleared by then.</p>
 */
class MainMenuControllerTest extends JavaFxTestBase {

    @AfterEach
    void clearCurrentUser() {
        User.setCurrentUser(null);
    }

    @Test
    void onActionLogoutResetsCurrentUserBeforeNavigating() {
        User.setCurrentUser(Fixtures.user());
        MainMenuController controller = new MainMenuController();

        // Navigation (displayScreen) cannot run headlessly and throws once the
        // current-user reset (the logic under test) has already happened.
        assertThrows(RuntimeException.class, () -> controller.onActionLogout(new ActionEvent()));

        assertNull(User.getCurrentUser(), "Logout must clear the current user before navigating");
    }
}
