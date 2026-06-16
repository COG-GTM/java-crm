# Java 8 → Java 11 Migration Notes

This document summarizes the migration of **java-crm** from Java 8 to Java 11 (LTS).

## Summary

The project was originally a NetBeans Ant-based JavaFX 8 application that relied on
the JavaFX runtime bundled with Oracle JDK 8. JavaFX was removed from the JDK in
Java 11, so the build was modernized to Maven and JavaFX is now pulled in as an
explicit OpenJFX dependency. Application behavior is unchanged.

## Build system

- Added a Maven `pom.xml` targeting Java 11 via `<maven.compiler.release>11</release>`.
- The original NetBeans source layout is preserved (`<sourceDirectory>src</sourceDirectory>`)
  to keep the diff minimal — no source files were moved. FXML, CSS, properties and image
  resources under `src/` are copied to the classpath via a `<resources>` entry.
- `maven-enforcer-plugin` requires a JDK 11+ toolchain.
- `maven-surefire`/`maven-failsafe`/`maven-javadoc` plugins pinned to Java 11-compatible versions.

Build and package:

```bash
mvn -B clean package
```

Run the application (requires a desktop with JavaFX-capable display):

```bash
mvn javafx:run
```

## Removed JDK modules replaced

- **JavaFX** — removed from the JDK in Java 11. Replaced with OpenJFX dependencies
  (`org.openjfx:javafx-controls` and `org.openjfx:javafx-fxml`, version `17.0.11`,
  which runs on Java 11+).

No usage of other removed/standalone modules (JAXB, JAX-WS, CORBA, Nashorn) was found.

## Code changes

- `src/DAO/DBConnection.java`: updated the JDBC driver class from the legacy
  `com.mysql.jdbc.Driver` to `com.mysql.cj.jdbc.Driver`, matching MySQL Connector/J 8.x
  (the legacy class was removed in Connector/J 8). This is a behavior-preserving change.

## Encapsulation / reflection (JPMS)

- The project builds and runs on the **classpath** (no `module-info.java`), which keeps
  the change minimal. No illegal reflective access warnings were observed during the build.

## Security / TLS / GC

- No application-level TLS configuration is present. Java 11 enables TLS 1.3 and defaults
  the keystore type to PKCS12; the MySQL endpoint connection is unaffected by these defaults.
- No custom GC flags are configured by the project; Java 11's default G1 GC applies.

## Baseline notes

- There are no automated unit/integration tests in the repository. `TestConditions.java`
  is a manual DB-backed helper, not a JUnit test, so the migration's validation is the
  successful compilation and packaging on JDK 11.
- The original Java 8 build depended on JavaFX bundled in **Oracle JDK 8**; OpenJDK 8 does
  not ship JavaFX, so the legacy Ant build cannot be reproduced on a stock OpenJDK 8.

## Validation

- `mvn -B clean package` succeeds on Temurin/OpenJDK 11.
- CI added at `.github/workflows/build.yml` builds the project on JDK 11.

## Follow-ups (out of scope)

- Move sources to the standard Maven layout (`src/main/java`, `src/main/resources`).
- Add automated tests.
- Externalize the hard-coded database credentials in `DBConnection.java`.
