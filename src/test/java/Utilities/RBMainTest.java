package Utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RBMainTest {

    private Locale originalLocale;

    @BeforeEach
    void captureLocale() {
        originalLocale = Locale.getDefault();
    }

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(originalLocale);
    }

    @Test
    void englishBundleIsUsedForEnglishLocale() {
        Locale.setDefault(Locale.ENGLISH);

        RBMain.setRb();

        assertEquals("Username", RBMain.getRb().getString("username"));
    }

    @Test
    void spanishBundleKeepsAccentedCharacters() {
        Locale.setDefault(Locale.forLanguageTag("es"));

        RBMain.setRb();
        ResourceBundle rb = RBMain.getRb();

        assertEquals("Nombre de usuario", rb.getString("username"));
        assertEquals("Contrase\u00f1a", rb.getString("password"));
    }

    @Test
    void unsupportedLocaleFallsBackToEnglish() {
        Locale.setDefault(Locale.JAPANESE);

        RBMain.setRb();

        assertEquals(Locale.ENGLISH, Locale.getDefault());
        assertEquals("Username", RBMain.getRb().getString("username"));
    }
}
