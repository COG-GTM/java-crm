/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import static DAO.DBQuery.setPreparedStatement;
import Model.Note;
import static Model.User.getCurrentUser;
import static Utilities.TimeFiles.dbStrNow;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Persistence layer for customer notes.
 *
 * Follows the same conventions as CustomerDaoImpl / UserDaoImpl: extends
 * GeneralDaoImpl, uses DBQuery.setPreparedStatement, and reads the shared
 * audit columns via getMetadata.
 */
public class NoteDaoImpl extends GeneralDaoImpl {

    //<editor-fold defaultstate="collapsed" desc="data retrieval">

    // Retrieve all notes for a given customer
    public static ObservableList<Note> getNotes(int customerId) throws SQLException {
        ObservableList<Note> notes = FXCollections.observableArrayList();

        String selectStatement = "SELECT * FROM note WHERE customerId = ? ORDER BY noteId";
        PreparedStatement ps = setPreparedStatement(selectStatement);
        ps.setInt(1, customerId);
        ps.execute();
        ResultSet rs = ps.getResultSet();

        while(rs.next()){
            int noteId = rs.getInt("noteId");
            String noteText = rs.getString("noteText");
            getMetadata(rs);
            Note note = new Note(noteId, customerId, noteText, createDateLdt, createdBy, lastUpdateLdt, lastUpdateBy);
            notes.add(note);
        }

        return notes;
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="data setting">

    // Insert a new note for a customer
    public static int insertNote(int customerId, String noteText) throws SQLException {
        String createStatement = "INSERT INTO note (customerId, noteText, createDate, createdBy, lastUpdate, lastUpdateBy) VALUES(?,?,?,?,?,?)";
        PreparedStatement ps = setPreparedStatement(createStatement);

        String userName = getCurrentUser().getUserName();
        String time = dbStrNow();

        ps.setInt(1, customerId);
        ps.setString(2, noteText);
        ps.setString(3, time);
        ps.setString(4, userName);
        ps.setString(5, time);
        ps.setString(6, userName);
        ps.execute();

        return ps.getUpdateCount();
    }

    // Update the text of an existing note
    public static int updateNote(String noteText, int noteId) throws SQLException {
        String updateStatement = "UPDATE note SET noteText = ?, lastUpdate = ?, lastUpdateBy = ? WHERE noteId = ?";
        PreparedStatement ps = setPreparedStatement(updateStatement);

        String userName = getCurrentUser().getUserName();
        String time = dbStrNow();

        ps.setString(1, noteText);
        ps.setString(2, time);
        ps.setString(3, userName);
        ps.setInt(4, noteId);
        ps.execute();

        return ps.getUpdateCount();
    }

    // Delete a note
    public static int deleteNote(int noteId) throws SQLException {
        String deleteStatement = "DELETE FROM note WHERE noteId = ?";
        PreparedStatement ps = setPreparedStatement(deleteStatement);
        ps.setInt(1, noteId);
        ps.execute();
        return ps.getUpdateCount();
    }

    //</editor-fold>

}
