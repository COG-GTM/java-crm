package Model;

import java.time.LocalDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Unit tests for the {@link Note} model, covering the shared field accessors and
 * the note-text validation used by both the UI controller and the DAO layer.
 *
 * @author Austin Wong
 */
public class NoteTest {

    private Note sampleNote(String text){
        LocalDateTime now = LocalDateTime.of(2021, 3, 15, 10, 30);
        return new Note(1, 42, text, now, "test", now, "test");
    }

    @Test
    public void constructorAndGettersReturnConstructedValues(){
        LocalDateTime created = LocalDateTime.of(2021, 1, 1, 9, 0);
        LocalDateTime updated = LocalDateTime.of(2021, 2, 2, 12, 0);
        Note note = new Note(5, 7, "Hello", created, "alice", updated, "bob");

        assertEquals(5, note.getNoteId());
        assertEquals(7, note.getCustomerId());
        assertEquals("Hello", note.getNoteText());
        assertEquals(created, note.getCreateDate());
        assertEquals("alice", note.getCreatedBy());
        assertEquals(updated, note.getLastUpdate());
        assertEquals("bob", note.getLastUpdateBy());
    }

    @Test
    public void settersUpdateValues(){
        Note note = sampleNote("original");
        note.setNoteId(99);
        note.setCustomerId(100);
        note.setNoteText("changed");
        note.setCreatedBy("carol");
        note.setLastUpdateBy("dave");

        assertEquals(99, note.getNoteId());
        assertEquals(100, note.getCustomerId());
        assertEquals("changed", note.getNoteText());
        assertEquals("carol", note.getCreatedBy());
        assertEquals("dave", note.getLastUpdateBy());
    }

    @Test
    public void isValidNoteTextRejectsNullEmptyAndWhitespace(){
        assertFalse(Note.isValidNoteText(null));
        assertFalse(Note.isValidNoteText(""));
        assertFalse(Note.isValidNoteText("   "));
        assertFalse(Note.isValidNoteText("\n\t "));
    }

    @Test
    public void isValidNoteTextAcceptsNormalText(){
        assertTrue(Note.isValidNoteText("A valid note."));
        assertTrue(Note.isValidNoteText("   trimmed still valid   "));
    }

    @Test
    public void isValidNoteTextEnforcesMaxLength(){
        StringBuilder atLimit = new StringBuilder();
        for(int i = 0; i < Note.MAX_NOTE_LENGTH; i++)
            atLimit.append("a");
        assertTrue(Note.isValidNoteText(atLimit.toString()));

        assertFalse(Note.isValidNoteText(atLimit.append("a").toString()));
    }

    @Test
    public void previewReturnsShortTextUnchanged(){
        assertEquals("Short note", sampleNote("Short note").getPreview());
    }

    @Test
    public void previewCollapsesWhitespaceAndTruncatesLongText(){
        StringBuilder longText = new StringBuilder();
        for(int i = 0; i < 200; i++)
            longText.append("x");
        String preview = sampleNote(longText.toString()).getPreview();
        assertEquals(60, preview.length());
        assertTrue(preview.endsWith("..."));
    }

    @Test
    public void previewHandlesNullText(){
        assertEquals("", sampleNote(null).getPreview());
    }
}
