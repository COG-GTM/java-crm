/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a free-text note attached to a customer.
 *
 * Mirrors the structure of the other model POJOs (Customer, Appointment):
 * private db fields, a constructor, getters/setters, and a static
 * "currentNote" reference used to pass the selected note between screens.
 */
public class Note {

    //<editor-fold defaultstate="collapsed" desc="static variables and methods">
    private static Note currentNote;

    public static Note getCurrentNote(){
        return currentNote;
    }

    public static void setCurrentNote(Note note){
        currentNote = note;
    }
    //</editor-fold>

    // Maximum number of characters allowed for a note's text
    public static final int MAX_NOTE_LENGTH = 1000;

    // Display format for audit timestamps (kept free of JavaFX/Utilities so the
    // model can be unit tested without the JavaFX toolkit or a database)
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");

    //<editor-fold defaultstate="collapsed" desc="db variables">
    private int noteId;
    private int customerId;
    private String noteText;
    private LocalDateTime createDate;
    private String createdBy;
    private LocalDateTime lastUpdate;
    private String lastUpdateBy;
    //</editor-fold>

    // Constructor
    public Note(int noteId, int customerId, String noteText, LocalDateTime createDate, String createdBy, LocalDateTime lastUpdate, String lastUpdateBy){
        this.noteId = noteId;
        this.customerId = customerId;
        this.noteText = noteText;
        this.createDate = createDate;
        this.createdBy = createdBy;
        this.lastUpdate = lastUpdate;
        this.lastUpdateBy = lastUpdateBy;
    }

    //<editor-fold defaultstate="collapsed" desc="db variable setters and getters">

    public int getNoteId(){
        return noteId;
    }

    public void setNoteId(int noteId){
        this.noteId = noteId;
    }

    public int getCustomerId(){
        return customerId;
    }

    public void setCustomerId(int customerId){
        this.customerId = customerId;
    }

    public String getNoteText(){
        return noteText;
    }

    public void setNoteText(String noteText){
        this.noteText = noteText;
    }

    public LocalDateTime getCreateDate(){
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate){
        this.createDate = createDate;
    }

    public String getCreatedBy(){
        return createdBy;
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastUpdate(){
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate){
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdateBy(){
        return lastUpdateBy;
    }

    public void setLastUpdateBy(String lastUpdateBy){
        this.lastUpdateBy = lastUpdateBy;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="derived getters for table display">

    // User-friendly created date used by the notes TableView
    public String getCreateDateDisplay(){
        if(createDate == null)
            return "";
        return createDate.format(DISPLAY_FORMAT);
    }

    // User-friendly last-updated date used by the notes TableView
    public String getLastUpdateDisplay(){
        if(lastUpdate == null)
            return "";
        return lastUpdate.format(DISPLAY_FORMAT);
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="validation">

    // Returns true if the supplied note text is non-empty and within the length limit
    public static boolean isValidNoteText(String noteText){
        return validateNoteText(noteText) == null;
    }

    // Validates note text, returning an error message describing the first problem
    // found or null if the text is valid. Used by the controller to surface a
    // message to the user and kept static so the rule can be unit tested directly.
    public static String validateNoteText(String noteText){
        if(noteText == null || noteText.trim().isEmpty())
            return "Note text cannot be empty";
        if(noteText.length() > MAX_NOTE_LENGTH)
            return "Note text cannot exceed " + MAX_NOTE_LENGTH + " characters";
        return null;
    }

    //</editor-fold>

    // Override for table/ComboBox display: show a short preview of the note text
    @Override
    public String toString(){
        if(noteText == null)
            return "Note [" + noteId + "]";
        String preview = noteText.length() > 40 ? noteText.substring(0, 40) + "..." : noteText;
        return preview + " [" + noteId + "]";
    }
}
