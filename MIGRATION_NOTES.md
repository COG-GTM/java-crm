# Java 8 → 11 Migration Notes

## Summary

This project has been migrated from Java 8 to Java 11 (LTS). The migration involved introducing a Maven build system, replacing JDK-bundled JavaFX with OpenJFX, updating the MySQL JDBC driver, and restructuring the source tree to the Maven standard layout.

## Changes

### Build System

| Before | After |
|--------|-------|
| No build tool (IDE-managed) | Maven with `pom.xml` |
| Java 8 target | `<maven.compiler.release>11</maven.compiler.release>` |
| No dependency management | Explicit Maven dependencies |

**Key plugins:**
- `maven-compiler-plugin` 3.11.0 — `release=11`
- `maven-surefire-plugin` 3.2.5
- `maven-failsafe-plugin` 3.2.5
- `maven-enforcer-plugin` 3.5.0 — requires JDK 11+
- `javafx-maven-plugin` 0.0.8 — for running the JavaFX application

### Removed JDK Modules — JavaFX

JavaFX was removed from the JDK starting with JDK 11. This project now uses **OpenJFX 17.0.11** as external Maven dependencies:

- `org.openjfx:javafx-controls:17.0.11`
- `org.openjfx:javafx-fxml:17.0.11`

No code changes were required; the `javafx.*` package names remain the same.

### MySQL JDBC Driver

| Before | After |
|--------|-------|
| `com.mysql.jdbc.Driver` (deprecated) | `com.mysql.cj.jdbc.Driver` |
| Driver assumed on classpath | `mysql:mysql-connector-java:8.0.33` in `pom.xml` |

### Source Layout

The project was restructured from a flat `src/` layout to the Maven standard:

```
src/main/java/       ← Java source files
src/main/resources/  ← FXML views, CSS, images, i18n properties
```

### CI/CD

A new GitHub Actions workflow (`.github/workflows/java11-build.yml`) builds and tests on **Temurin JDK 11** with Maven dependency caching.

## Not Applicable

The following migration concerns did not apply to this project:

- **JAXB / JAX-WS / CORBA**: not used
- **Nashorn**: not used
- **JPMS module-info.java**: project runs on the classpath (no modularization needed)
- **Illegal reflective access**: no reflection-dependent libraries detected
- **TLS/Security changes**: no custom TLS/keystore configuration
- **GC logging flags**: no JVM tuning flags were in use

## Running the Application

```bash
# Compile
mvn clean compile

# Run
mvn javafx:run

# Package
mvn clean package
```

## Follow-up Items

- Consider migrating package names from uppercase (`Controller`, `DAO`, `Model`) to lowercase per Java conventions
- Add unit tests
- Evaluate moving from `javax.*` MySQL driver to a connection pool (HikariCP)
