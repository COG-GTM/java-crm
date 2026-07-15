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

import Model.City;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

class CityDaoImplTest {

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
    }

    @Test
    void getCity_byId_mapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("city")).thenReturn("Phoenix");
            when(rs.getInt("countryId")).thenReturn(3);
            stubAuditColumns(rs);

            City c = CityDaoImpl.getCity(7);

            assertEquals(7, c.getCityId());
            assertEquals("Phoenix", c.getCityName());
            assertEquals(3, c.getCountryId());
            verify(ps).setInt(1, 7);
        }
    }

    @Test
    void getCity_byId_returnsNull_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertNull(CityDaoImpl.getCity(404));
        }
    }

    @Test
    void getCity_byNameAndCountry_mapsRow_andBindsParams() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("cityId")).thenReturn(11);
            when(rs.getString("city")).thenReturn("Denver");
            stubAuditColumns(rs);

            City c = CityDaoImpl.getCity("denver", 3);

            assertEquals(11, c.getCityId());
            assertEquals("Denver", c.getCityName());
            assertEquals(3, c.getCountryId());
            verify(ps).setString(1, "denver");
            verify(ps).setInt(2, 3);
        }
    }

    @Test
    void insertCity_bindsColumns_andReturnsInsertedCity() throws Exception {
        User.setCurrentUser(Fixtures.user(1, "admin"));
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("cityId")).thenReturn(20);
            when(rs.getString("city")).thenReturn("Austin");
            stubAuditColumns(rs);

            City c = CityDaoImpl.insertCity("Austin", 3);

            assertEquals(20, c.getCityId());
            assertEquals("Austin", c.getCityName());
            assertEquals(3, c.getCountryId());
            verify(ps, atLeastOnce()).setString(1, "Austin");
            verify(ps, atLeastOnce()).setInt(2, 3);
            verify(ps).setString(4, "admin");
            verify(ps).setString(6, "admin");
            verify(ps, times(2)).execute();
        }
    }
}
