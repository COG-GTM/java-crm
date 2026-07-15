package Utilities;

import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link RBMain} (Group D — Utilities), the resource-bundle
 * loader. The default {@link Locale} is saved and restored around each test.
 *
 * <p>Keys/values asserted here come from {@code src/Utilities/Nat_en.properties}
 * and {@code Nat_es.properties}.</p>
 */
class RBMainTest {

    private Locale originalLocale;

    @BeforeEach
    void saveLocale() {
        originalLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    void loadsEnglishBundle() {
        Locale.setDefault(Locale.ENGLISH);
        RBMain.setRb();

        ResourceBundle rb = RBMain.getRb();
        assertNotNull(rb);
        assertEquals("Username", rb.getString("username"));
        assertEquals("Password", rb.getString("password"));
    }

    @Test
    void loadsSpanishBundle() {
        Locale.setDefault(new Locale("es"));
        RBMain.setRb();

        ResourceBundle rb = RBMain.getRb();
        assertNotNull(rb);
        assertEquals("Nombre de usuario", rb.getString("username"));
    }

    @Test
    void fallsBackToEnglishForUnsupportedLocale() {
        Locale.setDefault(Locale.FRENCH);
        RBMain.setRb();

        // setRb() forces the default locale back to English for non en/es.
        assertEquals("en", Locale.getDefault().getLanguage());

        ResourceBundle rb = RBMain.getRb();
        assertNotNull(rb);
        assertEquals("Username", rb.getString("username"));
    }
}
