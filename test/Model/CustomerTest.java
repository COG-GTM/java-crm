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

class CustomerTest {

    @BeforeEach
    @AfterEach
    void resetStatic() {
        Customer.setCurrentCustomer(null);
    }

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime created = LocalDateTime.of(2020, 5, 1, 9, 30);
        LocalDateTime updated = LocalDateTime.of(2021, 6, 2, 10, 45);
        Customer customer = new Customer(3, "Acme", 8, true, created, "creator", updated, "editor");

        assertEquals(3, customer.getCustomerId());
        assertEquals("Acme", customer.getCustomerName());
        assertEquals(8, customer.getAddressId());
        assertTrue(customer.isActive());
        assertEquals(created, customer.getCreateDate());
        assertEquals("creator", customer.getCreatedBy());
        assertEquals(updated, customer.getLastUpdate());
        assertEquals("editor", customer.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        Customer customer = Fixtures.customer();
        LocalDateTime created = LocalDateTime.of(2019, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2019, 2, 2, 0, 0);

        customer.setCustomerId(55);
        customer.setCustomerName("Globex");
        customer.setAddressId(21);
        customer.setActive(false);
        customer.setCreateDate(created);
        customer.setCreatedBy("c2");
        customer.setLastUpdate(updated);
        customer.setLastUpdateBy("u2");

        assertEquals(55, customer.getCustomerId());
        assertEquals("Globex", customer.getCustomerName());
        assertEquals(21, customer.getAddressId());
        assertFalse(customer.isActive());
        assertEquals(created, customer.getCreateDate());
        assertEquals("c2", customer.getCreatedBy());
        assertEquals(updated, customer.getLastUpdate());
        assertEquals("u2", customer.getLastUpdateBy());
    }

    @Test
    void currentCustomer_roundTrips() {
        assertNull(Customer.getCurrentCustomer());

        Customer customer = Fixtures.customer();
        Customer.setCurrentCustomer(customer);
        assertSame(customer, Customer.getCurrentCustomer());

        Customer.setCurrentCustomer(null);
        assertNull(Customer.getCurrentCustomer());
    }

    @Test
    void toString_formatsNameAndId() {
        Customer customer = Fixtures.customer(9, "Initech");
        assertEquals("Initech [9]", customer.toString());
    }

    @Test
    void addressDelegationGetters_readFromWiredGraph() {
        Customer customer = Fixtures.customerWithFullAddress();

        assertEquals("123 Test St", customer.getAddress());
        assertEquals("Apt 2", customer.getAddress2());
        assertEquals("555-0100", customer.getPhone());
        assertEquals("00000", customer.getPostalCode());
        assertEquals("Test City", customer.getCity());
        assertEquals("Test Country", customer.getCountry());
    }
}
