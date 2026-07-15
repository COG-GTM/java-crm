package Utilities;

import Model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import testsupport.Fixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LogFiles} (Group D — Utilities).
 *
 * <p>{@code userlog.txt}/{@code errorlog.txt} are written to the working
 * directory (and are gitignored); they are deleted before and after each test
 * so nothing is left behind.</p>
 */
class LogFilesTest {

    private static final Path USER_LOG = Paths.get("userlog.txt");
    private static final Path ERROR_LOG = Paths.get("errorlog.txt");

    @BeforeEach
    void cleanUpBefore() throws IOException {
        Files.deleteIfExists(USER_LOG);
    }

    @AfterEach
    void cleanUpAfter() throws IOException {
        Files.deleteIfExists(USER_LOG);
        Files.deleteIfExists(ERROR_LOG);
    }

    @Test
    void logUserActivityWritesUserNameToUserLog() throws IOException {
        User user = Fixtures.user();
        User.setCurrentUser(user);

        LogFiles.logUserActivity();

        assertTrue(Files.exists(USER_LOG), "userlog.txt should be created");
        String contents = new String(Files.readAllBytes(USER_LOG));
        assertTrue(contents.contains(user.getUserName()),
                "log should contain the current user's name");
    }

    @Test
    void logUserActivityAppendsToExistingLog() throws IOException {
        User.setCurrentUser(Fixtures.user());

        LogFiles.logUserActivity();
        LogFiles.logUserActivity();

        long lines = Files.readAllLines(USER_LOG).stream()
                .filter(l -> !l.isEmpty())
                .count();
        assertTrue(lines >= 2, "second call should append another log line");
    }

    @Test
    void setupLoggerDoesNotThrow() {
        assertDoesNotThrow(LogFiles::setupLogger);
    }
}
