/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import DAO.NoteDaoImpl;
import Model.Customer;
import Model.Note;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controls the screen for listing, reading, adding, and editing a customer's
 * notes. Notes are scoped to the customer set via Customer.setCurrentCustomer
 * before this screen is displayed.
 *
 * @author Austin Wong
 */
public class NotesScreenController extends GeneralController implements Initializable {

    //<editor-fold defaultstate="collapsed" desc="ui-variables">

    @FXML
    private Label customerNameLbl;

    @FXML
    private TableView<Note> noteTableView;

    @FXML
    private TableColumn<?, ?> idCol;

    @FXML
    private TableColumn<?, ?> noteCol;

    @FXML
    private TableColumn<?, ?> createdByCol;

    @FXML
    private TableColumn<?, ?> lastUpdateCol;

    @FXML
    private TextArea noteTextArea;

    @FXML
    private Button newBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button backBtn;

    @FXML
    private Label errorLbl;

    //</editor-fold>

    private Customer customer;
    private final ObservableList<Note> notes = FXCollections.observableArrayList();
    private TableView.TableViewSelectionModel<Note> tvSelNote;

    // Query the DB to repopulate the notes table
    private void refreshTable(){
        notes.clear();
        try{
            if(customer != null)
                notes.addAll(NoteDaoImpl.getNotes(customer.getCustomerId()));
        }
        catch(SQLException e){
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            displayErrorAlert("Error retrieving notes from database");
        }
        noteTableView.setItems(notes);
        tvSelNote = noteTableView.getSelectionModel();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        customer = Customer.getCurrentCustomer();
        if(customer != null)
            customerNameLbl.setText("Notes for " + customer.getCustomerName());

        // Populate table columns
        idCol.setCellValueFactory(new PropertyValueFactory<>("noteId"));
        noteCol.setCellValueFactory(new PropertyValueFactory<>("noteText"));
        createdByCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdateDisplay"));

        refreshTable();

        // Load the selected note's text into the editor for reading/editing
        noteTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if(newSel != null)
                noteTextArea.setText(newSel.getNoteText());
        });
    }

    //<editor-fold defaultstate="collapsed" desc="actions">

    // Clear the editor to begin adding a new note
    @FXML
    void onActionNew(ActionEvent event) {
        noteTableView.getSelectionModel().clearSelection();
        noteTextArea.clear();
        noteTextArea.requestFocus();
    }

    // Save the editor's text as either a new note (no selection) or an update
    @FXML
    void onActionSave(ActionEvent event) {

        if(customer == null){
            displayErrorAlert("No customer selected");
            return;
        }

        String noteText = noteTextArea.getText();

        // Validate note text (non-empty, within length limit)
        String validationError = Note.validateNoteText(noteText);
        if(validationError != null){
            displayMessage(errorLbl, validationError);
            return;
        }

        try{
            if(tvSelNote.isEmpty()){
                NoteDaoImpl.insertNote(customer.getCustomerId(), noteText.trim());
                displayNotification(event, "Note added");
            }
            else{
                Note selected = tvSelNote.getSelectedItem();
                NoteDaoImpl.updateNote(noteText.trim(), selected.getNoteId());
                displayNotification(event, "Note updated");
            }
            noteTextArea.clear();
            refreshTable();
        }
        catch(SQLException e){
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            displayErrorAlert("Error saving note to database");
        }
    }

    // Delete the selected note after confirmation
    @FXML
    void onActionDelete(ActionEvent event) {
        if(tvSelNote.isEmpty()){
            displayErrorAlert("Select a note first");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete selected note?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait()
            .filter(res -> res == ButtonType.YES) // Only delete if the user confirms
            .ifPresent(res -> {
                try {
                    NoteDaoImpl.deleteNote(tvSelNote.getSelectedItem().getNoteId());
                    noteTextArea.clear();
                    displayNotification(event, "Note deleted");
                    refreshTable();
                }
                catch (SQLException e) {
                    Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
                    displayErrorAlert("Error deleting note from database");
                }
            });
    }

    // Return to previous screen
    @FXML
    void onActionReturn(ActionEvent event) {
        back(event);
    }

    //</editor-fold>

    private void back(Event event){
        returnToLastScreen(event);
    }
}
