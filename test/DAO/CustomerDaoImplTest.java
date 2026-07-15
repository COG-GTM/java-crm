package DAO;

import static DAO.DaoTestSupport.stubAuditColumns;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import Model.Customer;
import Model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

class CustomerDaoImplTest {

    @AfterEach
    void tearDown() {
        User.setCurrentUser(null);
    }

    @Test
    void getCustomer_byId_mapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getString("customerName")).thenReturn("Acme Corp");
            when(rs.getInt("addressId")).thenReturn(6);
            when(rs.getBoolean("active")).thenReturn(true);
            stubAuditColumns(rs);

            Customer c = CustomerDaoImpl.getCustomer(2);

            assertEquals(2, c.getCustomerId());
            assertEquals("Acme Corp", c.getCustomerName());
            assertEquals(6, c.getAddressId());
            assertTrue(c.isActive());
            verify(ps).setInt(1, 2);
        }
    }

    @Test
    void getCustomer_byId_returnsNull_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            assertNull(CustomerDaoImpl.getCustomer(77));
        }
    }

    @Test
    void getCustomer_byNameAndAddress_bindsParams_andMapsRow() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("customerId")).thenReturn(42);
            when(rs.getBoolean("active")).thenReturn(true);
            stubAuditColumns(rs);

            Customer c = CustomerDaoImpl.getCustomer("Acme Corp", 6);

            assertEquals(42, c.getCustomerId());
            assertEquals("Acme Corp", c.getCustomerName());
            assertEquals(6, c.getAddressId());
            verify(ps).setString(1, "Acme Corp");
            verify(ps).setInt(2, 6);
        }
    }

    @Test
    void getAllActiveCustomers_returnsEmptyList_whenNoRows() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            ObservableList<Customer> list = CustomerDaoImpl.getAllActiveCustomers();

            assertTrue(list.isEmpty());
        }
    }

    @Test
    void insertCustomer_bindsColumns_andReturnsInsertedCustomer() throws Exception {
        User.setCurrentUser(Fixtures.user(1, "admin"));
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getResultSet()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("customerId")).thenReturn(50);
            when(rs.getBoolean("active")).thenReturn(true);
            stubAuditColumns(rs);

            Customer c = CustomerDaoImpl.insertCustomer("New Co", 6);

            assertEquals(50, c.getCustomerId());
            assertEquals("New Co", c.getCustomerName());
            verify(ps, atLeastOnce()).setString(1, "New Co");
            verify(ps).setBoolean(3, true);
            verify(ps).setString(5, "admin");
            verify(ps).setString(7, "admin");
            verify(ps, times(2)).execute();
        }
    }

    @Test
    void deleteCustomer_bindsId_andReturnsUpdateCount() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getUpdateCount()).thenReturn(1);

            int rows = CustomerDaoImpl.deleteCustomer(8);

            assertEquals(1, rows);
            // deletes linked appointments then the customer -> id bound twice
            verify(ps, times(2)).setInt(1, 8);
            verify(ps, times(2)).execute();
        }
    }

    @Test
    void updateCustomer_bindsColumns_andReturnsUpdateCount() throws Exception {
        try (MockedStatic<DBQuery> dbq = mockStatic(DBQuery.class)) {
            PreparedStatement ps = mock(PreparedStatement.class);
            dbq.when(() -> DBQuery.setPreparedStatement(anyString())).thenReturn(ps);
            when(ps.getUpdateCount()).thenReturn(1);

            int rows = CustomerDaoImpl.updateCustomer("Renamed Co", 6, 8);

            assertEquals(1, rows);
            verify(ps).setString(1, "Renamed Co");
            verify(ps).setInt(2, 6);
            verify(ps).setInt(3, 8);
        }
    }
}
