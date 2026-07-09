# Java CRM

## Description
This Java application serves as a customer relationship management (CRM) system. It allows users to add, update, and remove customer data; manage appointments in a calendar; and run reports on appointment data.

This application was developed to fulfill the requirements for an Advanced Java Concepts academic project at WGU. It demonstrates database and file server application development skills. The application incorporates lambda expressions; advanced exception control mechanisms to improve user experience and application stability; localization and date/time APIs to support end-users in various geographical regions; and streams and filters to manipulate data more efficiently.

## Installation
Prerequisites:
* Download and install [Java Runtime Environment (JRE) 9](https://www.oracle.com/java/technologies/downloads/#java9) or higher

Steps:
1.	Install the prerequisite applications
2.	Download [dist.zip file](dist.zip)
3.	Extract files and run java-crm.jar

## Building from Source
The project targets **Java 9** and builds with Maven using the classpath approach
(no `module-info.java`). JavaFX is used from the classpath rather than the module path
to avoid reflective-access restrictions on the FXML loader.

Prerequisites:
* JDK 9
* Maven 3.6+

Build and package:
```bash
mvn clean package
```

Run:
```bash
mvn exec:java
# or run the packaged jar
java -jar target/java-crm.jar
```

### JavaFX note
JavaFX ships inside Oracle and BellSoft Liberica "full" JDK 9 runtimes, so on those
JDKs no external JavaFX dependency is required. Some JDK 9 builds (plain OpenJDK,
Azul Zulu) do **not** bundle JavaFX; on those, supply a JavaFX 8/9 `jfxrt.jar` via the
`local-javafx` profile:
```bash
mvn -Plocal-javafx -Djfxrt.jar=/path/to/jfxrt.jar clean package
```
When running on JDK 9, JavaFX/FXML emit "illegal reflective access" warnings — these are
expected and harmless on Java 9.

### MySQL driver
Database connectivity uses MySQL Connector/J 8.x (`com.mysql.cj.jdbc.Driver`), pulled in
automatically by Maven.

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
