# Java 8 → 11 Migration Notes

## Summary

This project was migrated from Java 8 to Java 11 (LTS) with minimal behavior
change. The migration introduces a Maven build, replaces the JDK-bundled JavaFX
(removed in JDK 11) with external OpenJFX dependencies, modernizes the MySQL
JDBC driver, and adds JDK 11 CI. The application is built and run on the
**classpath** (non-modular); no `module-info.java` is used.

## Java 8 Baseline

Before changing any targets, a Java 8 baseline was captured from the original
flat `src/` tree (pre-migration `main`).

- **JDK**: Azul Zulu FX 8 (`1.8.0_492`), which bundles JavaFX 8 the same way
  Oracle JDK 8 did. (Plain OpenJDK 8 does not ship JavaFX, which is the core
  reason this migration is required.)
- **Command**: `javac -encoding UTF-8 -Xlint:all -d out <all 33 sources>`
- **Result**: compiles cleanly (exit 0, 35 `.class` files).
- **Warnings (2)** — identical on Java 11, confirming parity:
  - `DBConnection.java`: redundant cast to `java.sql.Connection`
  - `BusinessException.java`: serializable class with no `serialVersionUID`
- **Tests**: none exist in the project (no test sources), so there is no test
  baseline to compare. Coverage is therefore unchanged (0 → 0).

## Build System

- **Before**: No build tool; the project was compiled in the IDE (NetBeans)
  against Oracle/Zulu JDK 8 with bundled JavaFX.
- **After**: Maven build with `maven-compiler-plugin` targeting
  `<release>11</release>`, UTF-8 encoding, and `-Xlint:all`.
- Plugins:
  - `maven-compiler-plugin` 3.11.0 (`<release>11</release>`, `-Xlint:all`)
  - `maven-surefire-plugin` 3.2.5, `maven-failsafe-plugin` 3.2.5 (managed)
  - `maven-enforcer-plugin` 3.5.0 — `requireJavaVersion [11,)`
  - `maven-javadoc-plugin` 3.6.3 (`-Xdoclint:none`, managed)
  - `maven-jar-plugin` 3.4.1 — sets `Main-Class: javacrm.Launcher`
  - `javafx-maven-plugin` 0.0.8 — convenience `mvn javafx:run`

## Project Structure

Source files moved from flat `src/` to the Maven standard layout:

| Before | After |
|--------|-------|
| `src/<package>/*.java` | `src/main/java/<package>/*.java` |
| `src/View/*.fxml` | `src/main/resources/View/*.fxml` |
| `src/Resources/*` | `src/main/resources/Resources/*` |
| `src/Utilities/*.properties` | `src/main/resources/Utilities/*.properties` |

## Removed JDK Module Replacements

### JavaFX (removed from the JDK in 11)

JavaFX shipped inside Oracle JDK 8 but was removed from the JDK in 11. It is now
provided by OpenJFX on the classpath:

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

No other removed JDK modules are used by this code base — there are **no**
imports of JAXB (`javax.xml.bind`), JAX-WS, `java.activation`, or CORBA
(`org.omg.*`), so no replacement dependencies were added for those.

### Classpath approach (no `module-info.java`)

Per the migration playbook, the project runs on the **classpath** (non-modular)
rather than the Java Platform Module System (JPMS). This avoids brittle
automatic-module names (e.g. the MySQL Connector/J jar has no
`Automatic-Module-Name`, so its JPMS name would be derived from the file name)
and the need for `opens` directives for FXML reflective access.

Because launching a class that extends `javafx.application.Application` directly
from the classpath fails with *"JavaFX runtime components are missing"*, a thin
`javacrm.Launcher` class (which does **not** extend `Application`) is the entry
point / `Main-Class` and delegates to `JavaCRM.main(...)`. This is the standard
non-modular JavaFX launch pattern.

No `--add-opens` flags are required.

## JDBC Driver Update

- **Before**: `com.mysql.jdbc.Driver` (removed in Connector/J 8)
- **After**: `com.mysql.cj.jdbc.Driver` (MySQL Connector/J 8.0.33, managed by Maven)

The driver class name is the only DB-related code change; the JDBC URL,
credentials handling, and SQL are unchanged.

## Resource Loading

Now that resources are on the classpath, loading was switched from relative
string paths to classpath resolution:

- **Before**: `scene.getStylesheets().add("Resources/generalStylesheet.css")`
- **After**: `scene.getStylesheets().add(getClass().getResource("/Resources/generalStylesheet.css").toExternalForm())`

(`GeneralController` and `JavaCRM` updated; FXML is loaded via
`getClass().getResource("/View/...")`.)

## Security / TLS

- Java 11 enables TLS 1.3 by default. The MySQL connection uses the standard
  handshake; if a legacy endpoint requires it, pin TLS 1.2 temporarily with
  `-Djdk.tls.client.protocols=TLSv1.2` (document and remove later).
- The default keystore type is PKCS12 (was JKS in Java 8). The app does not ship
  or read a keystore, so no conversion was needed.

## GC & Logging

- Default GC is G1 (since Java 9). The project used no explicit GC flags, so
  nothing needed to be removed.
- If GC logging is added, use Unified Logging:
  `-Xlog:gc*:file=gc.log:time,uptime,level,tags`.

## CI/CD

Added GitHub Actions workflow (`.github/workflows/java11-build.yml`):
- `actions/setup-java@v4`, `distribution: temurin`, `java-version: '11'`,
  `cache: maven`.
- Runs `mvn -B -e clean verify` (compiles, runs enforcer, packages the jar;
  there are no tests to run yet).

## Validation Evidence

| Check | Java 8 baseline | Java 11 |
|-------|-----------------|---------|
| Compile | clean (exit 0) | clean (`mvn clean package`, exit 0) |
| Warnings | 2 (cast, serial) | 2 (same) |
| Tests | none | none |
| Run | n/a (IDE) | `mvn javafx:run` launches the login window on the classpath; no "runtime components missing" |

## Known Issues / Notes

- No unit test suite exists; test coverage cannot be compared (unchanged at 0).
- The application connects to a hardcoded external MySQL database; runtime DB
  operations depend on that server being reachable. The login UI still renders
  when the DB is unreachable.
- The packaged jar (`target/java-crm-1.0-SNAPSHOT.jar`) is **not** self-contained
  (OpenJFX is not shaded in). Run with `mvn javafx:run`, or supply the OpenJFX
  modules on the module/class path manually.

## Follow-up Opportunities

- Add a JUnit 5 test suite.
- Optionally migrate to the `jakarta.*` namespace ahead of a future Java upgrade.
- Optional, mechanical-only modernizations: `var` (10), `Files.readString` (11).
- Consider `jlink`/`jpackage` for a self-contained distribution.
