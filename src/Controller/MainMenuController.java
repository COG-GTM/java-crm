/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import Model.Role;
import Model.User;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

/**
 * Controls main menu logic
 *
 * @author Austin Wong
 */
public class MainMenuController extends GeneralController implements Initializable {

    //<editor-fold defaultstate="collapsed" desc="ui-variables">
    
    @FXML
    private Button customersBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private Button appointmentsBtn;
    
    @FXML
    private Button reportsBtn;
    
    //</editor-fold>

    // Enable/disable menu options based on the current user's role.
    // Reports are gated by the centralized Role.VIEW_REPORTS policy. The
    // customers and appointments screens remain reachable for viewing by all
    // roles; the add/update/delete actions on those screens are individually
    // guarded via hasPermission(...), so READ_ONLY users cannot mutate data.
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        boolean canViewReports = hasPermission(Role.VIEW_REPORTS);
        reportsBtn.setDisable(!canViewReports);
        reportsBtn.setVisible(canViewReports);
        reportsBtn.setManaged(canViewReports);
    }

    //<editor-fold defaultstate="collapsed" desc="actions">
    
    @FXML
    void onActionDisplayCustomers(ActionEvent event) {
       displayScreen(event, "/View/ViewCustomerScreen.fxml");
    }

    @FXML
    void onActionDisplayAppointments(ActionEvent event) {
       displayScreen(event, "/View/CalendarScreen.fxml");
    }
    
    @FXML
    void onActionDisplayReports(ActionEvent event) {
        displayScreen(event, "/View/ReportScreen.fxml");
    }

    @FXML
    void onActionLogout(ActionEvent event) {
        User.setCurrentUser(null);
        displayScreen(event, "/View/LoginScreen.fxml");
    }
    
    //</editor-fold>
    
}
