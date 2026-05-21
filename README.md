# Java CRM

## Description
This Java application serves as a customer relationship management (CRM) system. It allows users to add, update, and remove customer data; manage appointments in a calendar; and run reports on appointment data.

This application was developed to fulfill the requirements for an Advanced Java Concepts academic project at WGU. It demonstrates database and file server application development skills. The application incorporates lambda expressions; advanced exception control mechanisms to improve user experience and application stability; localization and date/time APIs to support end-users in various geographical regions; and streams and filters to manipulate data more efficiently.

## Requirements
* **JDK 11** or higher ([Eclipse Temurin](https://adoptium.net/) recommended)
* **Apache Maven 3.6+**

## Building

```bash
mvn clean compile
```

## Running

```bash
mvn javafx:run
```

> **Note:** The application requires a MySQL database connection. See the database configuration in `src/main/java/DAO/DBConnection.java`.

## Installation (Pre-built JAR)
1. Download [dist.zip file](dist.zip)
2. Extract files and run java-crm.jar (requires JDK 11+)

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

## Project Status
This project was created for an academic course. Development will be discontinued for the foreseeable future.

## Migration Notes
This project was migrated from Java 8 to Java 11. See [MIGRATION_NOTES.md](MIGRATION_NOTES.md) for details.
