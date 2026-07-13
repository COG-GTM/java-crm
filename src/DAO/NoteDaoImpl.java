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
 * Data access for customer notes.
 *
 * @author Austin Wong
 */
public class NoteDaoImpl extends GeneralDaoImpl {

    //<editor-fold defaultstate="collapsed" desc="data retrieval">

    // Retrieve a single note from DB with noteId
    public static Note getNote(int noteId) throws SQLException {

        String sqlStatement = "SELECT * FROM note WHERE noteId = ?";
        PreparedStatement ps = setPreparedStatement(sqlStatement);
        ps.setInt(1, noteId);
        ps.execute();
        ResultSet rs = ps.getResultSet();

        while(rs.next()){
            int customerId = rs.getInt("customerId");
            String noteText = rs.getString("noteText");
            getMetadata(rs);
            return new Note(noteId, customerId, noteText, createDateLdt, createdBy, lastUpdateLdt, lastUpdateBy);
        }

        return null;
    }

    // Get a list of all notes for a given customer
    public static ObservableList<Note> getNotesByCustomer(int customerId) throws SQLException {
        ObservableList<Note> notes = FXCollections.observableArrayList();

        String selectStatement = "SELECT * FROM note WHERE customerId = ? ORDER BY lastUpdate DESC";
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

    // Insert note into DB
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

    // Update note in DB
    public static int updateNote(int noteId, String noteText) throws SQLException {
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

    // Delete note from DB
    public static int deleteNote(int noteId) throws SQLException {
        String deleteStatement = "DELETE FROM note WHERE noteId = ?";
        PreparedStatement ps = setPreparedStatement(deleteStatement);
        ps.setInt(1, noteId);
        ps.execute();
        return ps.getUpdateCount();
    }

    // Delete all notes for a given customer
    // Mirrors the in-application customer -> appointment cascade in CustomerDaoImpl
    public static int deleteNotesByCustomer(int customerId) throws SQLException {
        String deleteStatement = "DELETE FROM note WHERE customerId = ?";
        PreparedStatement ps = setPreparedStatement(deleteStatement);
        ps.setInt(1, customerId);
        ps.execute();
        return ps.getUpdateCount();
    }

    //</editor-fold>
}
