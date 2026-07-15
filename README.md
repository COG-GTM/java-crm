# Java CRM

## Description
This Java application serves as a customer relationship management (CRM) system. It allows users to add, update, and remove customer data; manage appointments in a calendar; and run reports on appointment data.

This application was developed to fulfill the requirements for an Advanced Java Concepts academic project at WGU. It demonstrates database and file server application development skills. The application incorporates lambda expressions; advanced exception control mechanisms to improve user experience and application stability; localization and date/time APIs to support end-users in various geographical regions; and streams and filters to manipulate data more efficiently.

## Installation
Prerequisites:
* Download and install [Java Runtime Environment (JRE) 8](https://www.oracle.com/java/technologies/javase-jre8-downloads.html) or higher

Steps:
1.	Install the prerequisite applications
2.	Download [dist.zip file](dist.zip)
3.	Extract files and run java-crm.jar

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


## Testing
This project includes a JUnit 5 (Jupiter) unit test suite driven by Maven, with Mockito for mocking (including static methods).

Prerequisites:
* JDK 11 or higher (JavaFX is pulled in as a Maven dependency, so no separate JavaFX SDK is required)
* Maven 3.6+

Layout:
* Production sources live in `src/` (non-standard NetBeans layout, configured via `pom.xml`).
* Tests live in `test/`, mirroring the production package structure (`Model`, `DAO`, `Controller`, `Utilities`).
* Shared fixtures/builders for sample model objects live in the `testsupport` package (`test/testsupport/Fixtures.java`).

Run the full test suite:
```
mvn test
```

Run a single test class:
```
mvn test -Dtest=UserTest
```

Coverage (97 tests across all layers):
* **Model** (`test/Model/`) — POJO constructors, getters/setters, `toString`, and static "current" state (`User`/`Customer`/`Appointment`).
* **DAO** (`test/DAO/`) — `UserDaoImpl`, `CountryDaoImpl`, `CityDaoImpl`, `AddressDaoImpl`, `CustomerDaoImpl`, `AppointmentDaoImpl`, `GeneralDaoImpl`; SQL parameter binding and result-row mapping verified against a mocked JDBC layer.
* **Controller** (`test/Controller/`) — non-UI logic such as `currentUserSelected()`, the report-dispatch switch, and the country/city/address/customer find-or-create flow, with DAO statics mocked.
* **Utilities** (`test/Utilities/`) — `TimeFiles` (date/time conversion + formatting), `BusinessException`, `RBMain` (i18n resource bundles), `LogFiles`.

Notes:
* The DAO layer's JDBC access (`Connection`, `PreparedStatement`, `ResultSet`) is mocked with Mockito; tests never touch a live database.
* Controller tests boot the JavaFX toolkit once (`test/Controller/JavaFxTestBase.java`) and run control-touching code on the JavaFX Application Thread; they use reflection to inject collaborators into private `@FXML` fields rather than refactoring production code. A JavaFX runtime is required to run the Controller tests.
* `Utilities/WindowSizing` is intentionally not unit-tested — it is pure `Screen`/`Stage`/`Alert` JavaFX code that requires a live display.

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
