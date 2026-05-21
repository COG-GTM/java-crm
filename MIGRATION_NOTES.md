# Java 8 to 11 Migration Notes

## Summary

This document describes the changes made to migrate the Java CRM application from Java 8 to Java 11 (LTS).

## Build System

**Before:** No formal build system; the project was developed in an IDE (NetBeans) with raw `.java` files and manual compilation.

**After:** Maven build system with:
- `maven-compiler-plugin` 3.11.0 with `<release>11</release>`
- `maven-surefire-plugin` 3.2.5
- `maven-failsafe-plugin` 3.2.5
- `maven-enforcer-plugin` 3.5.0 requiring JDK 11+
- `javafx-maven-plugin` 0.0.8 for JavaFX support
- `maven-javadoc-plugin` 3.6.3

## Project Structure

Source files reorganized from a flat `src/` layout to Maven standard directory layout:
- `src/main/java/` — Java source files
- `src/main/resources/` — FXML views, CSS stylesheets, images, and property files

## Key Changes

### JavaFX (Removed from JDK in Java 11)

JavaFX was bundled with the Oracle JDK 8 but was removed starting with JDK 11. The project now depends on **OpenJFX 17.0.11** as external Maven dependencies:
- `org.openjfx:javafx-controls`
- `org.openjfx:javafx-fxml`
- `org.openjfx:javafx-graphics`

### MySQL Connector

- **Before:** `com.mysql.jdbc.Driver` (legacy driver class, no explicit dependency management)
- **After:** `com.mysql.cj.jdbc.Driver` via `com.mysql:mysql-connector-j:8.0.33`

The legacy driver class `com.mysql.jdbc.Driver` was deprecated; the updated driver class `com.mysql.cj.jdbc.Driver` is the standard for MySQL Connector/J 8.x.

### JDK Removed Modules

| Module | Used? | Action |
|--------|-------|--------|
| JAXB (`javax.xml.bind`) | No | N/A |
| JAX-WS (`javax.xml.ws`) | No | N/A |
| CORBA | No | N/A |
| JavaFX | **Yes** | Replaced with OpenJFX (external dependency) |
| Nashorn | No | N/A |

### Encapsulation / Reflection

No illegal reflective access issues were identified. The application does not use reflection-heavy frameworks or bytecode manipulation libraries.

### Security / TLS

- Java 11 enables TLS 1.3 by default. The application connects to a MySQL database over JDBC; no custom TLS configuration was in place.
- Default keystore type changed from JKS to PKCS12 in Java 11. No custom keystores are used by this application.

### GC / Logging

- Java 11 defaults to G1 garbage collector (since Java 9). No custom GC flags were configured.
- Unified JVM Logging is available (since Java 9) but no GC logging flags were previously set.

## CI/CD

Added GitHub Actions workflow (`.github/workflows/java11-build.yml`) using:
- Temurin JDK 11
- Maven build with dependency caching

## Known Limitations

- The application connects to an external MySQL database at a hardcoded IP address. Database connectivity is required for full runtime testing but is not needed for compilation.
- No automated tests exist in the repository; test verification is limited to compilation.

## Follow-up Recommendations

1. **Externalize database configuration** — Move DB credentials and connection strings to environment variables or a configuration file.
2. **Add unit tests** — Introduce JUnit 5 for testable business logic (DAO layer, TimeFiles utilities).
3. **Consider Jakarta namespace migration** — If planning to move beyond Java 11 in the future, consider migrating from `javax.*` to `jakarta.*` APIs.
4. **Evaluate `var` usage** — Java 10 local variable type inference (`var`) could reduce verbosity in some areas.
