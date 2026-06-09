# Java CRM

## Description
This Java application serves as a customer relationship management (CRM) system. It allows users to add, update, and remove customer data; manage appointments in a calendar; and run reports on appointment data.

This application was developed to fulfill the requirements for an Advanced Java Concepts academic project at WGU. It demonstrates database and file server application development skills. The application incorporates lambda expressions; advanced exception control mechanisms to improve user experience and application stability; localization and date/time APIs to support end-users in various geographical regions; and streams and filters to manipulate data more efficiently.

## Prerequisites

* [JDK 11](https://adoptium.net/temurin/releases/?version=11) or higher (Temurin/Adoptium recommended)
* [Apache Maven](https://maven.apache.org/download.cgi) 3.6+

> JavaFX is no longer bundled with the JDK as of Java 11; it is pulled in as
> OpenJFX dependencies by Maven, so a plain JDK 11 (no bundled JavaFX) is fine.

## Building

```bash
mvn clean compile
```

## Running

```bash
mvn javafx:run
```

This is the recommended way to run the app: the `javafx-maven-plugin` configures
the OpenJFX runtime automatically.

## Packaging

```bash
mvn -DskipTests package
```

This produces `target/java-crm-1.0-SNAPSHOT.jar` (with `Main-Class: javacrm.Launcher`).
Note that this jar is **not** self-contained — the OpenJFX libraries are not
bundled into it. Prefer `mvn javafx:run`; to run the jar directly you must place
the OpenJFX modules on the module/class path yourself.

See [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for details of the Java 8 → 11 migration.

## Using the Application
The login screen will appear upon starting the application.

![Login Screen](img/login-screenshot.png)

Enter your credentials and click the "Login" button. Once logged in, the menu will appear.

![Menu Screen](img/menu-screen.png)

Use the buttons to navigate to other screens where you can view/manage customers, appointments, or reports.

![View Customers Screen](img/customer-list-screen.png)

![Add Customers](img/add-customer-screen.png)

![View Appointments Screen](img/calendar-screen.png)

![Add Appointments](img/add-appointment-screen.png)

![Reports Screen](img/reports-screen.png)


## Future Improvements
* Optimize colors and fonts for improved accessibility
* Improve design of reports screen
* Add ability to download/save reports
* Add section to README outlining how to test localization settings
* Add more detail to README under "Using the Application" section:
  * Explain screenshots
  * Explain required fields and input validation

## Migration Notes

This project was migrated from Java 8 to Java 11. See [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for details on what changed.

## Project Status
This project was created for an academic course. Development will be discontinued for the foreseeable future.
