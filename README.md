# Java CRM

## Description
This Java application serves as a customer relationship management (CRM) system. It allows users to add, update, and remove customer data; manage appointments in a calendar; and run reports on appointment data.

This application was developed to fulfill the requirements for an Advanced Java Concepts academic project at WGU. It demonstrates database and file server application development skills. The application incorporates lambda expressions; advanced exception control mechanisms to improve user experience and application stability; localization and date/time APIs to support end-users in various geographical regions; and streams and filters to manipulate data more efficiently.

## Installation
Prerequisites:
* A **JDK 9 that bundles JavaFX** (for example, Oracle JDK 9). JavaFX ships as part of these JDK 9 distributions, so no separate JavaFX SDK is required. (JavaFX was removed from the JDK starting with Java 11; running on Java 11+ would require adding the standalone OpenJFX modules.)
* [Apache Maven](https://maven.apache.org/) 3.6 or higher (only needed to build from source)

Steps:
1.	Install the prerequisite applications
2.	Download [dist.zip file](dist.zip)
3.	Extract files and run java-crm.jar

## Building and Running from Source
The project uses Maven. Sources live under `src/` (not the Maven-standard `src/main/java`), and the `pom.xml` is configured accordingly: it compiles with `--release 9`, treats `src/` as both the source and resource directory (so `.fxml`, `.css`, `.png`, and `.properties` files are copied to the build output), and depends on MySQL Connector/J.

Make sure `JAVA_HOME` points at a JDK 9 that bundles JavaFX, then:

```bash
# Compile
mvn clean compile

# Build the runnable jar (target/java-crm.jar)
mvn clean package

# Run the application
mvn exec:exec
```

`mvn exec:exec` launches a fresh JVM using `${java.home}/bin/java`; because the main class extends `javafx.application.Application`, the JDK 9 launcher resolves the bundled JavaFX modules automatically (they are also passed explicitly via `--add-modules javafx.controls,javafx.fxml`).

The application runs on the classpath (the unnamed module). On JDK 9 you may see illegal-reflective-access warnings from JavaFX's FXML injection; these are benign. A `module-info.java` was intentionally omitted to keep the migration minimal.

The application connects to a MySQL database on startup using MySQL Connector/J's `com.mysql.cj.jdbc.Driver`. If the database is unreachable the error is logged and the login screen still loads.

## Using the Application
The login screen will appear upon starting the application.

![Login Screen](img/login-screenshot.png)

Enter your credentials and click the “Login” button. Once logged in, the menu will appear.

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

## Project Status
This project was created for an academic course. Development will be discontinued for the foreseeable future.
