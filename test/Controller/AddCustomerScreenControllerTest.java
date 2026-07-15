package Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import DAO.AddressDaoImpl;
import DAO.CityDaoImpl;
import DAO.CountryDaoImpl;
import DAO.CustomerDaoImpl;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import testsupport.Fixtures;

/**
 * Unit tests for {@link AddCustomerScreenController}'s private
 * {@code submit(Event)} find-or-create logic.
 *
 * <p>For each of Country / City / Address / Customer the controller looks up the
 * entity via the corresponding {@code *DaoImpl.get*(...)} static and, when the
 * lookup returns {@code null}, creates it via {@code insert*(...)}. Real
 * {@code TextField}s (populated) and the {@code errorLbl Label} are injected into
 * the private {@code @FXML} fields via reflection and the four DAOs are stubbed
 * with {@code Mockito.mockStatic(...)}.</p>
 *
 * <p>Coverage:</p>
 * <ul>
 *   <li>All entities found -> no {@code insert*}, "already exists" message.</li>
 *   <li>Country/City/Address missing -> those {@code insert*} run (arguments are
 *       threaded from each created entity's id); Customer found -> no
 *       {@code insertCustomer}.</li>
 *   <li>All entities missing -> the full {@code insert*} chain runs including
 *       {@code insertCustomer}.</li>
 * </ul>
 *
 * <p>Not unit-tested (requires a live UI): the customer-created success path also
 * calls {@code displayNotification(event, ...)} and {@code back(event)}, which
 * cast {@code event.getSource()} to a JavaFX {@code Control} and load FXML /
 * manipulate a {@code Stage}. In the all-missing test the create path therefore
 * throws once the {@code insert*} chain (the logic under test) has completed;
 * that exception is asserted and the DAO interactions are then verified.</p>
 */
class AddCustomerScreenControllerTest extends JavaFxTestBase {

    private static final String COUNTRY = "Wonderland";
    private static final String CITY = "Capital";
    private static final String ADDRESS = "1 Main St";
    private static final String ADDRESS2 = "Suite 5";
    private static final String POSTAL = "12345";
    private static final String PHONE = "555-0100";
    private static final String CUSTOMER = "Acme Corp";

    private static final int COUNTRY_ID = 100;
    private static final int CITY_ID = 200;
    private static final int ADDRESS_ID = 300;

    private static AddCustomerScreenController newPopulatedController() {
        AddCustomerScreenController controller = new AddCustomerScreenController();
        setField(controller, "countryTxt", new TextField(COUNTRY));
        setField(controller, "cityTxt", new TextField(CITY));
        setField(controller, "addressTxt", new TextField(ADDRESS));
        setField(controller, "address2Txt", new TextField(ADDRESS2));
        setField(controller, "postalCodeTxt", new TextField(POSTAL));
        setField(controller, "phoneTxt", new TextField(PHONE));
        setField(controller, "customerNameTxt", new TextField(CUSTOMER));
        setField(controller, "errorLbl", new Label());
        return controller;
    }

    private static void invokeSubmit(AddCustomerScreenController controller, Event event) {
        invokePrivate(controller, "submit",
                new Class<?>[]{Event.class}, new Object[]{event});
    }

    @Test
    void submitTakesAlreadyExistsPathAndInsertsNothingWhenEverythingIsFound() throws Exception {
        runOnFxThread(() -> {
            AddCustomerScreenController controller = newPopulatedController();

            try (MockedStatic<CountryDaoImpl> countryMock = mockStatic(CountryDaoImpl.class);
                 MockedStatic<CityDaoImpl> cityMock = mockStatic(CityDaoImpl.class);
                 MockedStatic<AddressDaoImpl> addressMock = mockStatic(AddressDaoImpl.class);
                 MockedStatic<CustomerDaoImpl> customerMock = mockStatic(CustomerDaoImpl.class)) {

                countryMock.when(() -> CountryDaoImpl.getCountry(anyString()))
                        .thenReturn(Fixtures.country());
                cityMock.when(() -> CityDaoImpl.getCity(anyString(), anyInt()))
                        .thenReturn(Fixtures.city());
                addressMock.when(() -> AddressDaoImpl.getAddress(anyString(), anyString(), anyInt(),
                        anyString(), anyString())).thenReturn(Fixtures.address());
                customerMock.when(() -> CustomerDaoImpl.getCustomer(anyString(), anyInt()))
                        .thenReturn(Fixtures.customer());

                invokeSubmit(controller, new ActionEvent());

                countryMock.verify(() -> CountryDaoImpl.insertCountry(anyString()), never());
                cityMock.verify(() -> CityDaoImpl.insertCity(anyString(), anyInt()), never());
                addressMock.verify(() -> AddressDaoImpl.insertAddress(anyString(), anyString(),
                        anyInt(), anyString(), anyString()), never());
                customerMock.verify(() -> CustomerDaoImpl.insertCustomer(anyString(), anyInt()), never());

                Label errorLbl = (Label) getFieldValue(controller, "errorLbl");
                assertEquals("Customer already exists", errorLbl.getText());
            }
        });
    }

    @Test
    void submitCreatesMissingCountryCityAddressButNotCustomerWhenCustomerExists() throws Exception {
        runOnFxThread(() -> {
            AddCustomerScreenController controller = newPopulatedController();

            try (MockedStatic<CountryDaoImpl> countryMock = mockStatic(CountryDaoImpl.class);
                 MockedStatic<CityDaoImpl> cityMock = mockStatic(CityDaoImpl.class);
                 MockedStatic<AddressDaoImpl> addressMock = mockStatic(AddressDaoImpl.class);
                 MockedStatic<CustomerDaoImpl> customerMock = mockStatic(CustomerDaoImpl.class)) {

                stubFindReturnsNothingAndInsertReturnsFixtures(countryMock, cityMock, addressMock);
                // Customer already exists -> no insertCustomer, "already exists" path.
                customerMock.when(() -> CustomerDaoImpl.getCustomer(anyString(), anyInt()))
                        .thenReturn(Fixtures.customer());

                invokeSubmit(controller, new ActionEvent());

                countryMock.verify(() -> CountryDaoImpl.insertCountry(COUNTRY));
                cityMock.verify(() -> CityDaoImpl.insertCity(CITY, COUNTRY_ID));
                addressMock.verify(() -> AddressDaoImpl.insertAddress(ADDRESS, ADDRESS2, CITY_ID, POSTAL, PHONE));
                customerMock.verify(() -> CustomerDaoImpl.insertCustomer(anyString(), anyInt()), never());

                Label errorLbl = (Label) getFieldValue(controller, "errorLbl");
                assertEquals("Customer already exists", errorLbl.getText());
            }
        });
    }

    @Test
    void submitCreatesEntireCountryCityAddressCustomerChainWhenNothingExists() throws Exception {
        runOnFxThread(() -> {
            AddCustomerScreenController controller = newPopulatedController();

            try (MockedStatic<CountryDaoImpl> countryMock = mockStatic(CountryDaoImpl.class);
                 MockedStatic<CityDaoImpl> cityMock = mockStatic(CityDaoImpl.class);
                 MockedStatic<AddressDaoImpl> addressMock = mockStatic(AddressDaoImpl.class);
                 MockedStatic<CustomerDaoImpl> customerMock = mockStatic(CustomerDaoImpl.class)) {

                stubFindReturnsNothingAndInsertReturnsFixtures(countryMock, cityMock, addressMock);
                customerMock.when(() -> CustomerDaoImpl.getCustomer(anyString(), anyInt()))
                        .thenReturn(null);
                customerMock.when(() -> CustomerDaoImpl.insertCustomer(anyString(), anyInt()))
                        .thenReturn(Fixtures.customer());

                // The create-success path then calls displayNotification/back (UI/navigation),
                // which cannot run headlessly; the insert* chain under test has already executed.
                assertThrows(RuntimeException.class, () -> invokeSubmit(controller, new ActionEvent()));

                countryMock.verify(() -> CountryDaoImpl.insertCountry(COUNTRY));
                cityMock.verify(() -> CityDaoImpl.insertCity(CITY, COUNTRY_ID));
                addressMock.verify(() -> AddressDaoImpl.insertAddress(ADDRESS, ADDRESS2, CITY_ID, POSTAL, PHONE));
                customerMock.verify(() -> CustomerDaoImpl.insertCustomer(CUSTOMER, ADDRESS_ID));
            }
        });
    }

    private static void stubFindReturnsNothingAndInsertReturnsFixtures(
            MockedStatic<CountryDaoImpl> countryMock,
            MockedStatic<CityDaoImpl> cityMock,
            MockedStatic<AddressDaoImpl> addressMock) {
        countryMock.when(() -> CountryDaoImpl.getCountry(anyString())).thenReturn(null);
        countryMock.when(() -> CountryDaoImpl.insertCountry(anyString()))
                .thenReturn(Fixtures.country(COUNTRY_ID, COUNTRY));

        cityMock.when(() -> CityDaoImpl.getCity(anyString(), anyInt())).thenReturn(null);
        cityMock.when(() -> CityDaoImpl.insertCity(anyString(), anyInt()))
                .thenReturn(Fixtures.city(CITY_ID, CITY, COUNTRY_ID));

        addressMock.when(() -> AddressDaoImpl.getAddress(anyString(), anyString(), anyInt(),
                anyString(), anyString())).thenReturn(null);
        addressMock.when(() -> AddressDaoImpl.insertAddress(anyString(), anyString(), anyInt(),
                anyString(), anyString())).thenReturn(Fixtures.address(ADDRESS_ID, ADDRESS, CITY_ID));
    }

    private static Object getFieldValue(Object target, String fieldName) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to read field '" + fieldName + "'", e);
        }
    }
}
