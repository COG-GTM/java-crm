/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

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

    // Hide features the current user's role cannot access
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(!hasPermission(User.Permission.VIEW_REPORTS)){
            reportsBtn.setVisible(false);
            reportsBtn.setManaged(false);
        }
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
