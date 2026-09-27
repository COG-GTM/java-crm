import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Regression tests for the Maven/Java 21 restructure: they fail if a resource stops resolving
 * at the path the application code uses, if an FXML controller class is renamed or moved,
 * if the localization bundles drift apart, or if a database-less startup stops being safe.
 *
 * No JavaFX toolkit, no database and no network access are required.
 */
class MigrationResourcesTest {

    private static final String VIEW_DIRECTORY = "/View";
    private static final Pattern FXML_REFERENCE = Pattern.compile("\"(/View/[A-Za-z0-9_]+\\.fxml)\"");

    private static List<String> fxmlClasspathPaths() throws IOException {
        URL viewDirectory = MigrationResourcesTest.class.getResource(VIEW_DIRECTORY);
        assertNotNull(viewDirectory, VIEW_DIRECTORY + " is not on the classpath");

        Path directory = Path.of(java.net.URI.create(viewDirectory.toString()));
        try (Stream<Path> files = Files.list(directory)) {
            return files.map(file -> file.getFileName().toString())
                    .filter(name -> name.endsWith(".fxml"))
                    .sorted()
                    .map(name -> VIEW_DIRECTORY + "/" + name)
                    .collect(Collectors.toList());
        }
    }

    /** Every "/View/*.fxml" string literal that appears in the application sources. */
    private static Set<String> fxmlPathsUsedInCode() throws IOException {
        Path sources = Path.of("src", "main", "java");
        assertTrue(Files.isDirectory(sources), "application sources are not where the build expects them");

        Set<String> paths = new TreeSet<>();
        try (Stream<Path> files = Files.walk(sources)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList())) {
                Matcher matcher = FXML_REFERENCE.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    paths.add(matcher.group(1));
                }
            }
        }
        return paths;
    }

    /** "/View/CalendarScreen.fxml" for the AppScreen constant CALENDARSCREEN. */
    private static List<String> appScreenFxmlPaths() throws ReflectiveOperationException, IOException {
        Class<?> appScreen = Class.forName("Controller.GeneralController$AppScreen");
        Object[] constants = appScreen.getEnumConstants();
        assertNotNull(constants, "Controller.GeneralController.AppScreen is no longer an enum");
        assertTrue(constants.length > 0, "Controller.GeneralController.AppScreen has no constants");

        List<String> available = fxmlClasspathPaths();
        List<String> paths = new ArrayList<>();
        for (Object constant : constants) {
            String screen = constant.toString();
            paths.add(available.stream()
                    .filter(path -> path.substring(VIEW_DIRECTORY.length() + 1, path.length() - ".fxml".length())
                            .equalsIgnoreCase(screen))
                    .findFirst()
                    .orElse(VIEW_DIRECTORY + "/" + screen + ".fxml"));
        }
        return paths;
    }

    private static Document parse(String classpathResource) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (InputStream in = MigrationResourcesTest.class.getResourceAsStream(classpathResource)) {
            assertNotNull(in, classpathResource + " does not resolve on the classpath");
            return builder.parse(in);
        }
    }

    @Test
    void allNineScreensArePackagedUnderViewOnTheClasspath() throws IOException {
        List<String> screens = fxmlClasspathPaths();

        assertEquals(9, screens.size(), "expected the nine application screens, found " + screens);
        assertTrue(screens.contains("/View/LoginScreen.fxml"), "the login screen is missing: " + screens);
    }

    @ParameterizedTest(name = "{0} resolves on the classpath")
    @MethodSource("fxmlClasspathPaths")
    void packagedScreenResolvesAtItsClasspathPath(String screen) {
        assertNotNull(MigrationResourcesTest.class.getResource(screen), screen + " does not resolve");
    }

    @ParameterizedTest(name = "{0} is loaded by the application and resolves")
    @MethodSource("fxmlPathsUsedInCode")
    void screenPathUsedInCodeResolves(String screen) {
        assertNotNull(MigrationResourcesTest.class.getResource(screen),
                screen + " is loaded by the application but does not resolve on the classpath");
    }

    @Test
    void theLoginScreenIsLoadedByTheApplication() throws IOException {
        assertTrue(fxmlPathsUsedInCode().contains("/View/LoginScreen.fxml"));
    }

    @ParameterizedTest(name = "AppScreen path {0} resolves")
    @MethodSource("appScreenFxmlPaths")
    void appScreenEnumPathResolves(String screen) {
        assertNotNull(MigrationResourcesTest.class.getResource(screen),
                screen + " is referenced by GeneralController.AppScreen but does not resolve");
    }

    @Test
    void stylesheetResolvesThroughTheClassLoaderAtThePathTheSceneUses() {
        assertNotNull(MigrationResourcesTest.class.getClassLoader().getResource("Resources/generalStylesheet.css"),
                "Resources/generalStylesheet.css does not resolve; scene.getStylesheets() would be silently empty");
    }

    @Test
    void arrowImageResolvesThroughTheClassLoader() {
        assertNotNull(MigrationResourcesTest.class.getClassLoader().getResource("Resources/arrow.png"),
                "Resources/arrow.png does not resolve");
    }

    @ParameterizedTest(name = "{0} declares a loadable fx:controller")
    @MethodSource("fxmlClasspathPaths")
    void fxmlControllerClassExists(String screen) throws Exception {
        Element root = parse(screen).getDocumentElement();
        String controller = root.getAttribute("fx:controller");

        assertFalse(controller.isEmpty(), screen + " declares no fx:controller");
        assertDoesNotThrow(() -> Class.forName(controller),
                screen + " names controller " + controller + ", which no longer exists");
    }

    @Test
    void bothLocalizationBundlesResolve() {
        assertEquals("Username", ResourceBundle.getBundle("Utilities/Nat", Locale.ENGLISH).getString("username"));
        assertEquals("Nombre de usuario",
                ResourceBundle.getBundle("Utilities/Nat", Locale.forLanguageTag("es")).getString("username"));
    }

    /** Both property files carry their own file name as a stray key; it is not a translation. */
    private static Set<String> translationKeys(Locale locale) {
        return ResourceBundle.getBundle("Utilities/Nat", locale).keySet().stream()
                .filter(key -> !key.endsWith(".properties"))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Test
    void theTwoLocalizationBundlesShareTheSameKeys() {
        Set<String> english = translationKeys(Locale.ENGLISH);
        Set<String> spanish = translationKeys(Locale.forLanguageTag("es"));

        assertFalse(english.isEmpty(), "the English bundle is empty");
        assertEquals(english, spanish, "the English and Spanish bundles have drifted apart");
    }

    @Test
    void startConnectionReturnsNullWithoutDatabaseConfiguration() {
        assertNull(System.getenv("JAVACRM_DB_URL"), "this test requires an unconfigured environment");
        assertFalse(Files.exists(Path.of("db.properties")), "this test requires no db.properties in the basedir");

        assertNull(assertDoesNotThrow(DAO.DBConnection::startConnection),
                "an unconfigured startConnection() must fail softly and return null");
        assertNull(DAO.DBConnection.getConnection());
    }

    @Test
    void closeConnectionIsANoOpWhenThereIsNoConnection() {
        DAO.DBConnection.startConnection();

        assertDoesNotThrow(DAO.DBConnection::closeConnection);
    }
}
