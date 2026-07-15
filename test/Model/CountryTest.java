package Model;

import org.junit.jupiter.api.Test;
import testsupport.Fixtures;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CountryTest {

    @Test
    void getters_returnConstructorValues() {
        LocalDateTime created = LocalDateTime.of(2020, 5, 1, 9, 30);
        LocalDateTime updated = LocalDateTime.of(2021, 6, 2, 10, 45);
        Country country = new Country(2, "Wakanda", created, "creator", updated, "editor");

        assertEquals(2, country.getCountryId());
        assertEquals("Wakanda", country.getCountryName());
        assertEquals(created, country.getCreateDate());
        assertEquals("creator", country.getCreatedBy());
        assertEquals(updated, country.getLastUpdate());
        assertEquals("editor", country.getLastUpdateBy());
    }

    @Test
    void setters_updateValues() {
        Country country = Fixtures.country();
        LocalDateTime created = LocalDateTime.of(2019, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2019, 2, 2, 0, 0);

        country.setCountryId(44);
        country.setCountryName("Latveria");
        country.setCreateDate(created);
        country.setCreatedBy("c2");
        country.setLastUpdate(updated);
        country.setLastUpdateBy("u2");

        assertEquals(44, country.getCountryId());
        assertEquals("Latveria", country.getCountryName());
        assertEquals(created, country.getCreateDate());
        assertEquals("c2", country.getCreatedBy());
        assertEquals(updated, country.getLastUpdate());
        assertEquals("u2", country.getLastUpdateBy());
    }
}
