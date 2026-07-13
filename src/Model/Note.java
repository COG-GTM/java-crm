/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import static Utilities.TimeFiles.localDateTimeToUIDate;
import java.time.LocalDateTime;

/**
 * Represents a note associated with a customer.
 *
 * @author Austin Wong
 */
public class Note {

    //<editor-fold defaultstate="collapsed" desc="validation constants">
    // Maximum number of characters allowed in a note's text
    public static final int MAX_NOTE_LENGTH = 5000;
    //</editor-fold>

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

    // Returns user-friendly date the note was last updated
    // Used in notes table
    public String getLastUpdateDate(){
        return localDateTimeToUIDate(lastUpdate);
    }

    // Returns a shortened preview of the note text for display in the table
    public String getPreview(){
        if(noteText == null)
            return "";
        String singleLine = noteText.replaceAll("\\s+", " ").trim();
        if(singleLine.length() <= 60)
            return singleLine;
        return singleLine.substring(0, 57) + "...";
    }

    // Returns true if the supplied note text is valid (non-empty and within length limits)
    // Shared validation used by both the controller (UI) and DAO layers
    public static boolean isValidNoteText(String noteText){
        if(noteText == null)
            return false;
        String trimmed = noteText.trim();
        return !trimmed.isEmpty() && trimmed.length() <= MAX_NOTE_LENGTH;
    }
}
