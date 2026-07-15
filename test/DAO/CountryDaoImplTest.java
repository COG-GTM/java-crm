package DAO;

import static DAO.DaoTestSupport.stubAuditColumns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.Country;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

class CountryDaoImplTest {

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
    }

    @Test
    void getCountry_byId_mapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("country")).thenReturn("USA");
            stubAuditColumns(rs);

            Country c = CountryDaoImpl.getCountry(1);

            assertEquals(1, c.getCountryId());
            assertEquals("USA", c.getCountryName());
            assertEquals(Fixtures.TIMESTAMP, c.getCreateDate());
            assertEquals("test", c.getCreatedBy());
            verify(ps).setInt(1, 1);
        }
    }

    @Test
    void getCountry_byId_returnsNull_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertNull(CountryDaoImpl.getCountry(99));
            verify(ps).setInt(1, 99);
        }
    }

    @Test
    void getCountry_byName_mapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("countryId")).thenReturn(5);
            when(rs.getString("country")).thenReturn("Canada");
            stubAuditColumns(rs);

            Country c = CountryDaoImpl.getCountry("canada");

            assertEquals(5, c.getCountryId());
            assertEquals("Canada", c.getCountryName());
            verify(ps).setString(1, "canada");
        }
    }

    @Test
    void insertCountry_bindsColumns_andReturnsInsertedCountry() throws Exception {
        User.setCurrentUser(Fixtures.user(1, "admin"));
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            // no rows for INSERT.execute(); one row for the follow-up getCountry(name)
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("countryId")).thenReturn(10);
            when(rs.getString("country")).thenReturn("Mexico");
            stubAuditColumns(rs);

            Country c = CountryDaoImpl.insertCountry("Mexico");

            assertEquals(10, c.getCountryId());
            assertEquals("Mexico", c.getCountryName());
            // INSERT binds the country name at position 1 and the current user name
            verify(ps, atLeastOnce()).setString(1, "Mexico");
            verify(ps).setString(3, "admin");
            verify(ps).setString(5, "admin");
            // one execute for the INSERT, one for the follow-up SELECT
            verify(ps, times(2)).execute();
        }
    }
}
