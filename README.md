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

## Customer Notes
The application supports free-text notes attached to individual customers.

Each note stores the note text plus the standard audit columns
(`createDate`/`createdBy`/`lastUpdate`/`lastUpdateBy`) and is linked to a
customer via `customerId`.

### Using the Notes screen
1. From the **Main Menu**, click **Customers** to open the customer list.
2. Select a customer, then click **Notes**.
3. On the **Customer Notes** screen you can:
   * **Read** existing notes by selecting a row (the text loads into the editor).
   * **New** – clear the editor to write a new note, then **Save**.
   * **Save** – with a note selected this updates it; with no selection it adds a new note.
   * **Delete** – remove the selected note (after confirmation).
   * **Back** – return to the customer list.

Note text is validated before saving: it must be non-empty and at most
`Note.MAX_NOTE_LENGTH` (1000) characters.

Deleting a customer cascades to their notes: removing a customer deletes that
customer's appointments **and** notes.

### Database schema
The feature requires a `note` table. Apply the migration script against the
MySQL database configured in `src/DAO/DBConnection.java`:

```
mysql -h <host> -u <user> -p <database> < sql/note.sql
```

The script (`sql/note.sql`) creates the `note` table with a foreign key to
`customer(customerId)` and the audit columns.

## Testing
Unit and integration tests live under the `test/` directory, mirroring the
`src/` package layout, and use JUnit 4.

* **Unit tests** – `test/Model/NoteTest.java` covers the `Note` model and its
  note-text validation rules. These have no JavaFX or database dependency and
  can run standalone with only JUnit and Hamcrest on the classpath, e.g.:

  ```
  javac -cp junit-4.13.2.jar -d out src/Model/Note.java test/Model/NoteTest.java
  java  -cp out:junit-4.13.2.jar:hamcrest-core-1.3.jar org.junit.runner.JUnitCore Model.NoteTest
  ```

* **Integration tests** – `test/DAO/NoteDaoImplTest.java` exercises
  `NoteDaoImpl` CRUD against MySQL and verifies the cascade delete of notes when
  a customer is removed. These require network access to the configured MySQL
  server and the JavaFX runtime; if the database connection cannot be
  established the tests are skipped rather than failed. The MySQL JDBC driver
  (bundled in `dist.zip` under `dist/lib/`) must be on the classpath.

In NetBeans, add JUnit 4 to the project's test libraries and run the tests via
**Run > Test Project**.

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
