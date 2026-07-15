package testsupport;

import Model.Address;
import Model.Appointment;
import Model.City;
import Model.Country;
import Model.Customer;
import Model.Report;
import Model.User;

import java.time.LocalDateTime;

/**
 * Shared test fixtures / sample-object builders reused across all test groups
 * (Model, DAO, Controller, Utilities).
 *
 * <p>Every factory method returns a fully-populated object with sensible default
 * values so tests can either use them as-is or tweak individual fields via the
 * model setters. Overloads let callers pin the identifying fields they care
 * about (e.g. id + name) while leaving the audit columns at their defaults.</p>
 */
public final class Fixtures {

    /** Fixed timestamp used for all audit columns so tests are deterministic. */
    public static final LocalDateTime TIMESTAMP =
            LocalDateTime.of(2021, 1, 1, 12, 0, 0);

    public static final String DEFAULT_ACTOR = "test";

    private Fixtures() {
    }

    // <editor-fold desc="User">
    public static User user() {
        return user(1, "testuser");
    }

    public static User user(int userId, String userName) {
        return new User(userId, userName, "password", true,
                TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="Customer">
    public static Customer customer() {
        return customer(1, "Test Customer");
    }

    public static Customer customer(int customerId, String customerName) {
        return customer(customerId, customerName, 1);
    }

    public static Customer customer(int customerId, String customerName, int addressId) {
        return new Customer(customerId, customerName, addressId, true,
                TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="Address">
    public static Address address() {
        return address(1, "123 Test St", 1);
    }

    public static Address address(int addressId, String addressName, int cityId) {
        return new Address(addressId, addressName, "Apt 2", cityId, "00000",
                "555-0100", TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="City">
    public static City city() {
        return city(1, "Test City", 1);
    }

    public static City city(int cityId, String cityName, int countryId) {
        return new City(cityId, cityName, countryId,
                TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="Country">
    public static Country country() {
        return country(1, "Test Country");
    }

    public static Country country(int countryId, String countryName) {
        return new Country(countryId, countryName,
                TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="Appointment">
    public static Appointment appointment() {
        return appointment(1, 1, 1);
    }

    public static Appointment appointment(int appointmentId, int customerId, int userId) {
        return new Appointment(appointmentId, customerId, userId,
                "Test Appt", "Description", "Location", "Contact", "Consult",
                "http://example.com",
                TIMESTAMP, TIMESTAMP.plusHours(1),
                TIMESTAMP, DEFAULT_ACTOR, TIMESTAMP, DEFAULT_ACTOR);
    }
    // </editor-fold>

    // <editor-fold desc="Report">
    public static Report report() {
        return report("Test Report", 1);
    }

    public static Report report(String reportName, int reportId) {
        return new Report(reportName, reportId);
    }
    // </editor-fold>

    /**
     * Builds a fully-linked Customer -> Address -> City -> Country graph so
     * tests that traverse {@code Customer#getCity()}/{@code getCountry()} etc.
     * have the nested objects wired up without hitting the database.
     *
     * @return a Customer whose address/city/country object graph is populated
     */
    public static Customer customerWithFullAddress() {
        Country country = country();
        City city = city();
        setPrivateCountry(city, country);
        Address address = address();
        setPrivateCity(address, city);
        Customer customer = customer();
        setPrivateAddress(customer, address);
        return customer;
    }

    // The model classes only expose DAO-backed setters for their nested
    // objects, so reflection is used here (test-only) to wire the graph
    // without a live database.
    private static void setPrivateCountry(City city, Country country) {
        setField(city, "countryObj", country);
    }

    private static void setPrivateCity(Address address, City city) {
        setField(address, "cityObj", city);
    }

    private static void setPrivateAddress(Customer customer, Address address) {
        setField(customer, "addressObj", address);
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(
                    "Unable to set test fixture field '" + fieldName + "'", e);
        }
    }
}
