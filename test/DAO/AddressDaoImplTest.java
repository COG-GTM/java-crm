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

import Model.Address;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

class AddressDaoImplTest {

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
    }

    @Test
    void getAddress_byId_mapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("address")).thenReturn("1 Main St");
            when(rs.getString("address2")).thenReturn("Suite 5");
            when(rs.getInt("cityId")).thenReturn(4);
            when(rs.getString("postalCode")).thenReturn("85001");
            when(rs.getString("phone")).thenReturn("555-1234");
            stubAuditColumns(rs);

            Address a = AddressDaoImpl.getAddress(2);

            assertEquals(2, a.getAddressId());
            assertEquals("1 Main St", a.getAddressName());
            assertEquals("Suite 5", a.getAddress2Name());
            assertEquals(4, a.getCityId());
            assertEquals("85001", a.getPostalCode());
            assertEquals("555-1234", a.getPhone());
            verify(ps).setInt(1, 2);
        }
    }

    @Test
    void getAddress_byId_returnsNull_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertNull(AddressDaoImpl.getAddress(123));
        }
    }

    @Test
    void getAddress_byFields_bindsAllParams_andMapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("addressId")).thenReturn(9);
            stubAuditColumns(rs);

            Address a = AddressDaoImpl.getAddress("1 Main St", "Suite 5", 4, "85001", "555-1234");

            assertEquals(9, a.getAddressId());
            assertEquals("1 Main St", a.getAddressName());
            verify(ps).setString(1, "1 Main St");
            verify(ps).setString(2, "Suite 5");
            verify(ps).setInt(3, 4);
            verify(ps).setString(4, "85001");
            verify(ps).setString(5, "555-1234");
        }
    }

    @Test
    void insertAddress_bindsColumns_andReturnsInsertedAddress() throws Exception {
        User.setCurrentUser(Fixtures.user(1, "admin"));
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("addressId")).thenReturn(30);
            stubAuditColumns(rs);

            Address a = AddressDaoImpl.insertAddress("9 Oak Ave", "", 4, "12345", "555-9999");

            assertEquals(30, a.getAddressId());
            assertEquals("9 Oak Ave", a.getAddressName());
            verify(ps, atLeastOnce()).setString(1, "9 Oak Ave");
            verify(ps, atLeastOnce()).setInt(3, 4);
            verify(ps).setString(7, "admin");
            verify(ps).setString(9, "admin");
            verify(ps, times(2)).execute();
        }
    }
}
