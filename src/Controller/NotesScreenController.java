/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

import DAO.NoteDaoImpl;
import Model.Customer;
import Model.Note;
import Utilities.LogFiles;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
 * Controls screen for reading and editing customer notes
 *
 * @author Austin Wong
 */
public class NotesScreenController extends GeneralController implements Initializable {

    //<editor-fold defaultstate="collapsed" desc="ui-variables">

    @FXML
    private Label customerLbl;

    @FXML
    private TableView<Note> noteTableView;

    @FXML
    private TableColumn<?, ?> idCol;

    @FXML
    private TableColumn<?, ?> previewCol;

    @FXML
    private TableColumn<?, ?> createdByCol;

    @FXML
    private TableColumn<?, ?> lastUpdateCol;

    @FXML
    private TextArea noteTextArea;

    @FXML
    private Label errorLbl;

    @FXML
    private Button addBtn;

    @FXML
    private Button updateBtn;

    @FXML
    private Button deleteBtn;

    @FXML
    private Button backBtn;

    //</editor-fold>

    private final ObservableList<Note> notes = FXCollections.observableArrayList();
    private TableView.TableViewSelectionModel<Note> tvSelNote;
    private Customer customer;

    // Query the DB to repopulate table
    private void refreshTable(){
        notes.clear();
        try{
            notes.addAll(NoteDaoImpl.getNotesByCustomer(customer.getCustomerId()));
        }
        catch(SQLException e){
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            displayErrorAlert("Error retrieving notes from database");
        }
        noteTableView.setItems(notes);
        tvSelNote = noteTableView.getSelectionModel();
    }

    private void selectionError(){
        displayErrorAlert("Select a note first");
    }

    // Returns false if the note text in the text area is invalid, displaying a message
    private boolean validateNoteText(){
        String noteText = noteTextArea.getText();
        if(noteText == null || noteText.trim().isEmpty()){
            displayMessage(errorLbl, "Note text cannot be empty");
            return false;
        }
        if(!Note.isValidNoteText(noteText)){
            displayMessage(errorLbl, "Note text must be " + Note.MAX_NOTE_LENGTH + " characters or fewer");
            return false;
        }
        return true;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        customer = Customer.getCurrentCustomer();
        customerLbl.setText("Notes for " + customer.getCustomerName());

        // Populate table
        idCol.setCellValueFactory(new PropertyValueFactory<>("noteId"));
        previewCol.setCellValueFactory(new PropertyValueFactory<>("preview"));
        createdByCol.setCellValueFactory(new PropertyValueFactory<>("createdBy"));
        lastUpdateCol.setCellValueFactory(new PropertyValueFactory<>("lastUpdateDate"));
        refreshTable();

        // Load the full text of the selected note into the text area for reading/editing
        noteTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if(newNote != null)
                noteTextArea.setText(newNote.getNoteText());
        });
    }

    //<editor-fold defaultstate="collapsed" desc="actions">

    // Add a new note for the current customer
    @FXML
    void onActionAddNote(ActionEvent event) {
        if(!validateNoteText())
            return;
        try{
            NoteDaoImpl.insertNote(customer.getCustomerId(), noteTextArea.getText().trim());
            LogFiles.logNoteActivity("ADD", customer.getCustomerId());
            displayNotification(event, "Note added");
            noteTextArea.clear();
            refreshTable();
        }
        catch(SQLException e){
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            displayErrorAlert("Error adding note to database");
        }
    }

    // Update the selected note with the text in the text area
    @FXML
    void onActionUpdateNote(ActionEvent event) {
        if(tvSelNote.isEmpty()){
            selectionError();
            return;
        }
        if(!validateNoteText())
            return;
        try{
            int noteId = tvSelNote.getSelectedItem().getNoteId();
            NoteDaoImpl.updateNote(noteId, noteTextArea.getText().trim());
            LogFiles.logNoteActivity("UPDATE", customer.getCustomerId());
            displayNotification(event, "Note updated");
            refreshTable();
        }
        catch(SQLException e){
            Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
            displayErrorAlert("Error updating note in database");
        }
    }

    // Delete the selected note
    @FXML
    void onActionDeleteNote(ActionEvent event) {
        if(tvSelNote.isEmpty()){
            selectionError();
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete note?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait()
            .filter(res -> res == ButtonType.YES) // Using lambda to handle unique event where user confirms note deletion
            .ifPresent(res -> {
                try {
                    NoteDaoImpl.deleteNote(tvSelNote.getSelectedItem().getNoteId());
                    LogFiles.logNoteActivity("DELETE", customer.getCustomerId());
                    displayNotification(event, "Note deleted");
                    noteTextArea.clear();
                    refreshTable();
                }
                catch (SQLException e) {
                    Logger.getLogger("errorlog.txt").log(Level.WARNING, null, e);
                    displayErrorAlert("Error deleting note from database");
                }
            });
    }

    // Return to the customer list
    @FXML
    void onActionReturn(ActionEvent event) {
        displayScreen(event, "/View/ViewCustomerScreen.fxml");
    }

    //</editor-fold>
}
