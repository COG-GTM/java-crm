package DAO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection factory.
 *
 * Connection settings are read, in order of precedence, from:
 * <ol>
 *   <li>the environment variables JAVACRM_DB_URL, JAVACRM_DB_USER, JAVACRM_DB_PASSWORD</li>
 *   <li>a db.properties file on the working directory or on the classpath,
 *       with the keys db.url, db.user and db.password</li>
 * </ol>
 *
 * @author Austin Wong
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DEFAULT_CONNECTION_PARAMS =
            "serverTimezone=UTC&useSSL=true&allowPublicKeyRetrieval=true";

    private static Connection conn = null;

    public static Connection startConnection() {

        try {
            Properties config = loadConfig();

            String url = withConnectionParams(value(config, "db.url", "JAVACRM_DB_URL"));
            String username = value(config, "db.user", "JAVACRM_DB_USER");
            String password = value(config, "db.password", "JAVACRM_DB_PASSWORD");

            if (url == null || username == null || password == null) {
                throw new SQLException("Database configuration missing. Set JAVACRM_DB_URL, "
                        + "JAVACRM_DB_USER and JAVACRM_DB_PASSWORD, or provide a " + CONFIG_FILE + " file.");
            }

            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            Logger.getLogger("errorlog.txt").log(Level.SEVERE, null, e);
        }

        return conn;

    }

    public static Connection getConnection() {
        return conn;
    }

    public static void closeConnection() {

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    private static Properties loadConfig() {

        Properties config = new Properties();

        Path localFile = Path.of(CONFIG_FILE);
        if (Files.exists(localFile)) {
            try (InputStream in = Files.newInputStream(localFile)) {
                config.load(in);
                return config;
            } catch (IOException e) {
                Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            }
        }

        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                config.load(in);
            }
        } catch (IOException e) {
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
        }

        return config;

    }

    private static String value(Properties config, String key, String environmentVariable) {

        String fromEnvironment = System.getenv(environmentVariable);
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }

        String fromFile = config.getProperty(key);
        return (fromFile == null || fromFile.isBlank()) ? null : fromFile;

    }

    private static String withConnectionParams(String url) {

        if (url == null) {
            return null;
        }

        if (url.contains("serverTimezone=")) {
            return url;
        }

        return url + (url.contains("?") ? "&" : "?") + DEFAULT_CONNECTION_PARAMS;

    }
}
