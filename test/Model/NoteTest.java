/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Model;

import java.time.LocalDateTime;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the {@link Note} model and its note-text validation rules.
 * These tests are deliberately free of any JavaFX or database dependencies so
 * they can run standalone with only JUnit on the classpath.
 *
 * @author Austin Wong
 */
public class NoteTest {

    private final LocalDateTime sample = LocalDateTime.of(2021, 3, 15, 14, 30);

    @After
    public void tearDown() {
        // Reset the shared static reference so tests don't leak state
        Note.setCurrentNote(null);
    }

    @Test
    public void constructorAndGettersReturnSuppliedValues() {
        Note note = new Note(1, 42, "Hello world", sample, "alice", sample, "bob");

        assertEquals(1, note.getNoteId());
        assertEquals(42, note.getCustomerId());
        assertEquals("Hello world", note.getNoteText());
        assertEquals(sample, note.getCreateDate());
        assertEquals("alice", note.getCreatedBy());
        assertEquals(sample, note.getLastUpdate());
        assertEquals("bob", note.getLastUpdateBy());
    }

    @Test
    public void settersUpdateValues() {
        Note note = new Note(1, 42, "Hello", sample, "alice", sample, "bob");

        note.setNoteId(2);
        note.setCustomerId(99);
        note.setNoteText("Changed");
        note.setCreatedBy("carol");
        note.setLastUpdateBy("dave");

        assertEquals(2, note.getNoteId());
        assertEquals(99, note.getCustomerId());
        assertEquals("Changed", note.getNoteText());
        assertEquals("carol", note.getCreatedBy());
        assertEquals("dave", note.getLastUpdateBy());
    }

    @Test
    public void currentNoteStaticAccessorsRoundTrip() {
        assertNull(Note.getCurrentNote());
        Note note = new Note(5, 1, "x", sample, "a", sample, "a");
        Note.setCurrentNote(note);
        assertEquals(note, Note.getCurrentNote());
    }

    @Test
    public void validateNoteTextRejectsNull() {
        assertNotNull(Note.validateNoteText(null));
        assertFalse(Note.isValidNoteText(null));
    }

    @Test
    public void validateNoteTextRejectsEmptyAndWhitespace() {
        assertNotNull(Note.validateNoteText(""));
        assertNotNull(Note.validateNoteText("   "));
        assertFalse(Note.isValidNoteText(""));
        assertFalse(Note.isValidNoteText("\t \n"));
    }

    @Test
    public void validateNoteTextRejectsTooLong() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Note.MAX_NOTE_LENGTH + 1; i++) {
            sb.append("a");
        }
        assertNotNull(Note.validateNoteText(sb.toString()));
        assertFalse(Note.isValidNoteText(sb.toString()));
    }

    @Test
    public void validateNoteTextAcceptsValidText() {
        assertNull(Note.validateNoteText("A perfectly reasonable note"));
        assertTrue(Note.isValidNoteText("A perfectly reasonable note"));
    }

    @Test
    public void validateNoteTextAcceptsTextAtMaxLength() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Note.MAX_NOTE_LENGTH; i++) {
            sb.append("a");
        }
        assertTrue(Note.isValidNoteText(sb.toString()));
    }

    @Test
    public void createDateDisplayFormatsNonNullDate() {
        Note note = new Note(1, 1, "x", sample, "a", sample, "a");
        assertEquals("3/15/2021 2:30 PM", note.getCreateDateDisplay());
        assertEquals("3/15/2021 2:30 PM", note.getLastUpdateDisplay());
    }

    @Test
    public void displayGettersHandleNullDates() {
        Note note = new Note(1, 1, "x", null, "a", null, "a");
        assertEquals("", note.getCreateDateDisplay());
        assertEquals("", note.getLastUpdateDisplay());
    }

    @Test
    public void toStringTruncatesLongText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 60; i++) {
            sb.append("a");
        }
        Note note = new Note(7, 1, sb.toString(), sample, "a", sample, "a");
        String result = note.toString();
        assertTrue(result.contains("..."));
        assertTrue(result.contains("[7]"));
    }
}
