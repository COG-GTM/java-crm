package DAO;

import Controller.TestConditions;
import Model.Customer;
import Model.Note;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.collections.ObservableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * MySQL integration tests for {@link NoteDaoImpl}. These exercise real CRUD
 * against the {@code note} table using the same {@link DBConnection} setup the
 * application uses.
 *
 * The tests are skipped automatically (via {@link Assume}) when the database is
 * not reachable, so they never fail a build that runs without DB access.
 *
 * @author Austin Wong
 */
public class NoteDaoIntegrationTest {

    private static boolean dbAvailable;
    private static int customerId;

    @BeforeClass
    public static void setUpClass() throws Exception {
        // Bound the connection attempt so an unreachable DB skips quickly
        DriverManager.setLoginTimeout(5);
        Connection conn = DBConnection.startConnection();
        dbAvailable = conn != null;
        Assume.assumeTrue("Skipping NoteDaoImpl integration tests: database not reachable", dbAvailable);

        // Create a customer (and current user) to attach notes to
        TestConditions.createTestCustomer();
        customerId = Customer.getCurrentCustomer().getCustomerId();
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        if(dbAvailable){
            TestConditions.cleanUp();
            DBConnection.closeConnection();
        }
    }

    @Test
    public void fullCrudLifecycle() throws SQLException {
        // CREATE
        assertEquals(1, NoteDaoImpl.insertNote(customerId, "Integration test note"));

        // READ (list)
        ObservableList<Note> notes = NoteDaoImpl.getNotesByCustomer(customerId);
        assertTrue("Inserted note should be returned for the customer", notes.size() >= 1);
        Note inserted = notes.get(0); // ordered by lastUpdate DESC, newest first
        assertEquals("Integration test note", inserted.getNoteText());
        assertEquals(customerId, inserted.getCustomerId());

        // READ (single)
        Note fetched = NoteDaoImpl.getNote(inserted.getNoteId());
        assertNotNull(fetched);
        assertEquals(inserted.getNoteId(), fetched.getNoteId());

        // UPDATE
        assertEquals(1, NoteDaoImpl.updateNote(inserted.getNoteId(), "Updated integration note"));
        assertEquals("Updated integration note", NoteDaoImpl.getNote(inserted.getNoteId()).getNoteText());

        // DELETE
        assertEquals(1, NoteDaoImpl.deleteNote(inserted.getNoteId()));
        assertEquals(null, NoteDaoImpl.getNote(inserted.getNoteId()));
    }

    @Test
    public void deleteNotesByCustomerRemovesAllNotes() throws SQLException {
        NoteDaoImpl.insertNote(customerId, "note one");
        NoteDaoImpl.insertNote(customerId, "note two");
        assertTrue(NoteDaoImpl.getNotesByCustomer(customerId).size() >= 2);

        NoteDaoImpl.deleteNotesByCustomer(customerId);
        assertEquals(0, NoteDaoImpl.getNotesByCustomer(customerId).size());
    }
}
