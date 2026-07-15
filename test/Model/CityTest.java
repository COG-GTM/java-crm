package Model;

import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class CityTest {

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime created = LocalDateTime.of(2020, 5, 1, 9, 30);
        LocalDateTime updated = LocalDateTime.of(2021, 6, 2, 10, 45);
        City city = new City(6, "Gotham", 9, created, "creator", updated, "editor");

        assertEquals(6, city.getCityId());
        assertEquals("Gotham", city.getCityName());
        assertEquals(9, city.getCountryId());
        assertEquals(created, city.getCreateDate());
        assertEquals("creator", city.getCreatedBy());
        assertEquals(updated, city.getLastUpdate());
        assertEquals("editor", city.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        City city = Fixtures.city();
        LocalDateTime created = LocalDateTime.of(2019, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2019, 2, 2, 0, 0);

        city.setCityId(66);
        city.setCityName("Star City");
        city.setCountryId(77);
        city.setCreateDate(created);
        city.setCreatedBy("c2");
        city.setLastUpdate(updated);
        city.setLastUpdateBy("u2");

        assertEquals(66, city.getCityId());
        assertEquals("Star City", city.getCityName());
        assertEquals(77, city.getCountryId());
        assertEquals(created, city.getCreateDate());
        assertEquals("c2", city.getCreatedBy());
        assertEquals(updated, city.getLastUpdate());
        assertEquals("u2", city.getLastUpdateBy());
    }

    @Test
    void getCountryObj_returnsWiredCountry() throws Exception {
        City city = Fixtures.city();
        Country country = Fixtures.country(3, "Freedonia");
        Field field = City.class.getDeclaredField("countryObj");
        field.setAccessible(true);
        field.set(city, country);

        assertSame(country, city.getCountryObj());
    }
}
