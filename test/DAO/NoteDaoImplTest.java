/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DAO;

import Controller.TestConditions;
import Model.Customer;
import Model.Note;
import java.sql.SQLException;
import javafx.collections.ObservableList;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests exercising {@link NoteDaoImpl} CRUD operations against the
 * live MySQL database configured in {@link DBConnection}, and verifying the
 * cascade delete of notes when a customer is removed
 * ({@link CustomerDaoImpl#deleteCustomer(int)}).
 *
 * These tests require network access to the configured MySQL server. If the
 * connection cannot be established the tests are skipped (via {@link Assume})
 * rather than failed, so they can be run selectively in an environment that has
 * database access.
 *
 * A test customer is created through {@link TestConditions#createTestCustomer()}
 * (which also logs in the "test" user needed for the audit columns) and is
 * cleaned up after each test.
 *
 * @author Austin Wong
 */
public class NoteDaoImplTest {

    private int customerId;

    @BeforeClass
    public static void openConnection() {
        Assume.assumeNotNull("MySQL connection unavailable", DBConnection.startConnection());
    }

    @AfterClass
    public static void closeConnection() {
        DBConnection.closeConnection();
    }

    @Before
    public void createCustomer() throws Exception {
        TestConditions.createTestCustomer();
        customerId = Customer.getCurrentCustomer().getCustomerId();
    }

    @Test
    public void insertGetUpdateDeleteRoundTrip() throws SQLException {
        int before = NoteDaoImpl.getNotes(customerId).size();

        // Insert
        int inserted = NoteDaoImpl.insertNote(customerId, "Integration test note");
        assertEquals(1, inserted);

        ObservableList<Note> notes = NoteDaoImpl.getNotes(customerId);
        assertEquals(before + 1, notes.size());
        Note newNote = notes.get(notes.size() - 1);
        assertEquals("Integration test note", newNote.getNoteText());
        assertNotNull(newNote.getCreatedBy());

        // Update
        int updated = NoteDaoImpl.updateNote("Updated integration note", newNote.getNoteId());
        assertEquals(1, updated);
        Note reloaded = findNote(customerId, newNote.getNoteId());
        assertNotNull(reloaded);
        assertEquals("Updated integration note", reloaded.getNoteText());

        // Delete
        int deleted = NoteDaoImpl.deleteNote(newNote.getNoteId());
        assertEquals(1, deleted);
        assertEquals(before, NoteDaoImpl.getNotes(customerId).size());

        cleanupCustomer();
    }

    @Test
    public void deletingCustomerCascadesToNotes() throws SQLException {
        NoteDaoImpl.insertNote(customerId, "Note A");
        NoteDaoImpl.insertNote(customerId, "Note B");
        assertTrue(NoteDaoImpl.getNotes(customerId).size() >= 2);

        // Deleting the customer should also remove that customer's notes
        CustomerDaoImpl.deleteCustomer(customerId);
        Customer.setCurrentCustomer(null);

        assertEquals(0, NoteDaoImpl.getNotes(customerId).size());
    }

    private static Note findNote(int customerId, int noteId) throws SQLException {
        for (Note note : NoteDaoImpl.getNotes(customerId)) {
            if (note.getNoteId() == noteId) {
                return note;
            }
        }
        return null;
    }

    private void cleanupCustomer() throws SQLException {
        if (Customer.getCurrentCustomer() != null) {
            TestConditions.cleanUp();
        }
    }
}
