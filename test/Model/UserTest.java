package Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @BeforeEach
    @AfterEach
    void resetStatic() {
        User.setCurrentUser(null);
    }

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime created = LocalDateTime.of(2020, 5, 1, 9, 30);
        LocalDateTime updated = LocalDateTime.of(2021, 6, 2, 10, 45);
        User user = new User(7, "alice", "secret", true, created, "creator", updated, "editor");

        assertEquals(7, user.getUserId());
        assertEquals("alice", user.getUserName());
        assertEquals("secret", user.getPassword());
        assertTrue(user.isActive());
        assertEquals(created, user.getCreateDate());
        assertEquals("creator", user.getCreatedBy());
        assertEquals(updated, user.getLastUpdate());
        assertEquals("editor", user.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        User user = Fixtures.user();
        LocalDateTime created = LocalDateTime.of(2019, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2019, 2, 2, 0, 0);

        user.setUserId(99);
        user.setUserName("bob");
        user.setPassword("pw2");
        user.setActive(false);
        user.setCreateDate(created);
        user.setCreatedBy("c2");
        user.setLastUpdate(updated);
        user.setLastUpdateBy("u2");

        assertEquals(99, user.getUserId());
        assertEquals("bob", user.getUserName());
        assertEquals("pw2", user.getPassword());
        assertFalse(user.isActive());
        assertEquals(created, user.getCreateDate());
        assertEquals("c2", user.getCreatedBy());
        assertEquals(updated, user.getLastUpdate());
        assertEquals("u2", user.getLastUpdateBy());
    }

    @Test
    void currentUser_roundTrips() {
        assertNull(User.getCurrentUser());

        User user = Fixtures.user();
        User.setCurrentUser(user);
        assertSame(user, User.getCurrentUser());

        User.setCurrentUser(null);
        assertNull(User.getCurrentUser());
    }

    @Test
    void toString_formatsNameAndId() {
        User user = Fixtures.user(42, "charlie");
        assertEquals("charlie [42]", user.toString());
    }
}
