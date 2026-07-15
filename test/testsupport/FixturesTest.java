package testsupport;

import Model.Customer;
import Model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test proving the shared test foundation (JUnit 5 + fixtures) is wired
 * up correctly. Group-specific tests live alongside the packages they cover.
 */
class FixturesTest {

    @Test
    void buildsDefaultUser() {
        User user = Fixtures.user();
        assertEquals(1, user.getUserId());
        assertEquals("testuser", user.getUserName());
    }

    @Test
    void buildsCustomerWithFullAddressGraph() {
        Customer customer = Fixtures.customerWithFullAddress();
        assertNotNull(customer.getAddress());
        assertEquals("Test City", customer.getCity());
        assertEquals("Test Country", customer.getCountry());
    }
}
