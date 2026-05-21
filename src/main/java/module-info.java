module javacrm {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.logging;

    opens javacrm to javafx.fxml;
    opens Controller to javafx.fxml;
    opens Model to javafx.base, javafx.fxml;
    opens Utilities to javafx.fxml;

    exports javacrm;
}
