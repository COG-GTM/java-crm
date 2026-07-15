package DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class UserDaoImplTest {

    @BeforeEach
    void resetCurrentUser() {
        User.setCurrentUser(null);
    }

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
    }

    @Test
    void logIn_returnsTrue_andSetsCurrentUser_andBindsCredentials() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("userId")).thenReturn(7);

            Boolean result = UserDaoImpl.logIn("bob", "secret");

            assertTrue(result);
            assertEquals(7, User.getCurrentUser().getUserId());
            assertEquals("bob", User.getCurrentUser().getUserName());
            verify(ps).setString(1, "bob");
            verify(ps).setString(2, "secret");
        }
    }

    @Test
    void logIn_returnsFalse_andLeavesCurrentUserUnset_whenNoMatch() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Boolean result = UserDaoImpl.logIn("bob", "wrong");

            assertFalse(result);
            assertNull(User.getCurrentUser());
            verify(ps).setString(1, "bob");
            verify(ps).setString(2, "wrong");
        }
    }

    @Test
    void getAllActiveUsers_mapsMultipleRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);
            when(rs.getInt("userId")).thenReturn(1, 2);
            when(rs.getString("userName")).thenReturn("alice", "bob");
            when(rs.getBoolean("active")).thenReturn(true);

            ObservableList<User> users = UserDaoImpl.getAllActiveUsers();

            assertEquals(2, users.size());
            assertEquals("alice", users.get(0).getUserName());
            assertEquals(1, users.get(0).getUserId());
            assertEquals("bob", users.get(1).getUserName());
            assertEquals(2, users.get(1).getUserId());
        }
    }

    @Test
    void getAllActiveUsers_returnsEmptyList_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertTrue(UserDaoImpl.getAllActiveUsers().isEmpty());
        }
    }
}
