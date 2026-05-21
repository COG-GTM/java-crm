# Java 8 to 11 Migration Notes

## Summary

This project has been migrated from Java 8 to Java 11 (LTS). The migration includes
build system introduction (Maven), external JavaFX dependencies, updated JDBC driver,
and a proper modular project structure.

## Build System

- **Before**: No build tool; project was compiled via IDE (NetBeans) with JDK 8 bundled JavaFX.
- **After**: Maven-based build with `maven-compiler-plugin` targeting `<release>11</release>`.
- Plugins: `maven-compiler-plugin` 3.11.0, `maven-surefire-plugin` 3.2.5, `maven-enforcer-plugin` 3.5.0.
- Enforcer rule requires JDK 11+.

## Project Structure

Source files moved from flat `src/` to Maven standard layout:

| Before | After |
|--------|-------|
| `src/<package>/*.java` | `src/main/java/<package>/*.java` |
| `src/View/*.fxml` | `src/main/resources/View/*.fxml` |
| `src/Resources/*.css` | `src/main/resources/Resources/*.css` |
| `src/Utilities/*.properties` | `src/main/resources/Utilities/*.properties` |

## Removed JDK Module Replacements

### JavaFX (removed from JDK 11)

JavaFX was bundled with Oracle JDK 8 but removed in JDK 11. Replaced with OpenJFX:

```xml
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>11.0.2</version>
</dependency>
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-fxml</artifactId>
    <version>11.0.2</version>
</dependency>
```

The `javafx-maven-plugin` (0.0.8) is used to run the application with proper module configuration.

## JPMS Module System

A `module-info.java` was added because JavaFX 11+ requires module declarations for
FXML reflection access to controllers and model property binding:

```java
module javacrm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens javacrm to javafx.fxml;
    opens Controller to javafx.fxml;
    opens Model to javafx.base, javafx.fxml;
    opens Utilities to javafx.fxml;

    exports javacrm;
}
```

## JDBC Driver Update

- **Before**: `com.mysql.jdbc.Driver` (deprecated)
- **After**: `com.mysql.cj.jdbc.Driver` (MySQL Connector/J 8.0.33)

The MySQL Connector/J dependency is now managed via Maven instead of manual classpath
configuration.

## Resource Loading

Stylesheet loading updated from relative string paths to classpath-based resource
resolution:

- **Before**: `scene.getStylesheets().add("Resources/generalStylesheet.css")`
- **After**: `scene.getStylesheets().add(getClass().getResource("/Resources/generalStylesheet.css").toExternalForm())`

## CI/CD

Added GitHub Actions workflow (`.github/workflows/java11-build.yml`) using:
- Temurin JDK 11
- Maven build with caching
- Compilation verification on push/PR to main

## Security Notes

- TLS 1.3 is enabled by default in Java 11. The MySQL connection should work without
  changes, but if connecting to legacy endpoints, pin TLS 1.2 via
  `-Djdk.tls.client.protocols=TLSv1.2`.
- Default keystore type is now PKCS12 (was JKS in Java 8).

## GC Changes

- Default GC is G1 (since Java 9). No explicit GC flags were used previously.
- Legacy GC logging flags are not applicable; use Unified Logging if needed:
  `-Xlog:gc*:file=gc.log:time,uptime,level,tags`

## Known Issues

- No unit test suite exists; test coverage cannot be compared between Java 8 and 11.
- The application connects to a hardcoded external MySQL database; connectivity depends
  on that server being available.

## Follow-up Opportunities

- Add unit tests with JUnit 5
- Consider migrating to Jakarta namespace for future Java version upgrades
- Evaluate `var` (local variable type inference) for improved readability
- Consider `java.net.http.HttpClient` if HTTP calls are added in the future
