package Model;

import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AddressTest {

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime created = LocalDateTime.of(2020, 5, 1, 9, 30);
        LocalDateTime updated = LocalDateTime.of(2021, 6, 2, 10, 45);
        Address address = new Address(4, "1 Main St", "Suite 5", 12, "90210",
                "555-1234", created, "creator", updated, "editor");

        assertEquals(4, address.getAddressId());
        assertEquals("1 Main St", address.getAddressName());
        assertEquals("Suite 5", address.getAddress2Name());
        assertEquals(12, address.getCityId());
        assertEquals("90210", address.getPostalCode());
        assertEquals("555-1234", address.getPhone());
        assertEquals(created, address.getCreateDate());
        assertEquals("creator", address.getCreatedBy());
        assertEquals(updated, address.getLastUpdate());
        assertEquals("editor", address.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        Address address = Fixtures.address();
        LocalDateTime created = LocalDateTime.of(2019, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2019, 2, 2, 0, 0);

        address.setAddressId(77);
        address.setAddressName("2 Elm St");
        address.setAddress2Name("Floor 3");
        address.setCityId(88);
        address.setPostalCode("10001");
        address.setPhone("555-9999");
        address.setCreateDate(created);
        address.setCreatedBy("c2");
        address.setLastUpdate(updated);
        address.setLastUpdateBy("u2");

        assertEquals(77, address.getAddressId());
        assertEquals("2 Elm St", address.getAddressName());
        assertEquals("Floor 3", address.getAddress2Name());
        assertEquals(88, address.getCityId());
        assertEquals("10001", address.getPostalCode());
        assertEquals("555-9999", address.getPhone());
        assertEquals(created, address.getCreateDate());
        assertEquals("c2", address.getCreatedBy());
        assertEquals(updated, address.getLastUpdate());
        assertEquals("u2", address.getLastUpdateBy());
    }

    @Test
    void getCityObj_returnsWiredCity() throws Exception {
        Address address = Fixtures.address();
        City city = Fixtures.city(5, "Metropolis", 1);
        Field field = Address.class.getDeclaredField("cityObj");
        field.setAccessible(true);
        field.set(address, city);

        assertSame(city, address.getCityObj());
    }
}
