package dev.shreyaspatil.noty.repository.local

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.Either.Error
import dev.shreyaspatil.noty.core.repository.Either.Success
import dev.shreyaspatil.noty.data.local.dao.NotesDao
import dev.shreyaspatil.noty.data.local.entity.NoteEntity
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class NotyLocalNoteRepositoryTest {
    private lateinit var notesDao: NotesDao
    private lateinit var repository: NotyLocalNoteRepository

    private val testNote = Note(
        id = "UNIQUE_ID",
        title = "Lorem Ipsum",
        note = "This is body of a note!",
        created = Date().time,
        isPinned = false,
    )
    private val testNoteEntity = NoteEntity(testNote.id, testNote.title, testNote.note, testNote.created, testNote.isPinned)

    @BeforeEach
    fun setUp() {
        notesDao = mockk(relaxUnitFun = true)
        repository = NotyLocalNoteRepository(notesDao)
    }

    @Test
    fun `addNote with successful DAO operation should return temporary noteId and add to DAO`() = runTest {
        val response = repository.addNote(testNote.title, testNote.note)
        val noteId = (response as Success).data

        assertTrue(noteId.startsWith("TMP"))

        val actualNoteEntity = slot<NoteEntity>()
        coVerify { notesDao.addNote(capture(actualNoteEntity)) }

        assertEquals(testNoteEntity.title, actualNoteEntity.captured.title)
        assertEquals(testNoteEntity.note, actualNoteEntity.captured.note)
    }

    @Test
    fun `addNote with failed DAO operation should return error`() = runTest {
        coEvery { notesDao.addNote(any()) } throws Exception("")
        val response = repository.addNote(testNote.title, testNote.note)
        assertEquals("Unable to create a new note", (response as Error).message)
    }

    @Test
    fun `addNotes should add notes to DAO`() = runTest {
        repository.addNotes(listOf(testNote))
        coVerify { notesDao.addNotes(listOf(testNoteEntity)) }
    }

    @Test
    fun `getNoteById should return note from DAO`() = runTest {
        coEvery { notesDao.getNoteById(testNoteEntity.noteId) } returns flowOf(testNoteEntity)
        val actualNote = repository.getNoteById(testNoteEntity.noteId).first()

        assertEquals(testNoteEntity.noteId, actualNote.id)
        assertEquals(testNoteEntity.title, actualNote.title)
        assertEquals(testNoteEntity.note, actualNote.note)
        assertEquals(testNoteEntity.created, actualNote.created)
    }

    @Test
    fun `updateNote with successful DAO operation should update note in DAO`() = runTest {
        val newTitle = "New title"
        val newNote = "New note body"
        coEvery { notesDao.updateNoteById(any(), any(), any()) } just Runs

        repository.updateNote(testNoteEntity.noteId, newTitle, newNote)

        coVerify { notesDao.updateNoteById(testNoteEntity.noteId, newTitle, newNote) }
    }

    @Test
    fun `updateNote with failed DAO operation should return error`() = runTest {
        val newTitle = "New title"
        val newNote = "New note body"
        coEvery { notesDao.updateNoteById(any(), any(), any()) } throws Exception()

        val response = repository.updateNote(testNoteEntity.noteId, newTitle, newNote)
        assertEquals("Unable to update a note", (response as Error).message)
    }

    @Test
    fun `updateNoteId should update noteId in DAO`() = runTest {
        val newNoteId = "NEW_NOTE_ID"
        repository.updateNoteId(oldNoteId = testNoteEntity.noteId, newNoteId = newNoteId)
        coVerify { notesDao.updateNoteId(testNoteEntity.noteId, newNoteId) }
    }

    @Test
    fun `deleteNote with successful DAO operation should delete note in DAO`() = runTest {
        coEvery { notesDao.deleteNoteById(any()) } just Runs
        repository.deleteNote(testNoteEntity.noteId)
        coVerify { notesDao.deleteNoteById(testNoteEntity.noteId) }
    }

    @Test
    fun `deleteNote with failed DAO operation should return error`() = runTest {
        coEvery { notesDao.deleteNoteById(any()) } throws Exception()
        val response = repository.deleteNote(testNoteEntity.noteId)
        assertEquals("Unable to delete a note", (response as Error).message)
    }

    @Test
    fun `pinNote with successful DAO operation should pin note in DAO`() = runTest {
        coEvery { notesDao.updateNotePin(any(), any()) } just Runs
        repository.pinNote(testNoteEntity.noteId, true)
        coVerify { notesDao.updateNotePin(testNoteEntity.noteId, true) }
    }

    @Test
    fun `pinNote with failed DAO operation should return error`() = runTest {
        coEvery { notesDao.updateNotePin(any(), any()) } throws Exception()
        val response = repository.pinNote(testNoteEntity.noteId, false)
        assertEquals("Unable to pin the note", (response as Error).message)
    }

    @Test
    fun `getAllNotes should retrieve all notes from DAO`() = runTest {
        val noteEntities = listOf(testNoteEntity.copy(noteId = "1"), testNoteEntity.copy(noteId = "2"))
        coEvery { notesDao.getAllNotes() } returns flowOf(noteEntities)

        val notesResponse = repository.getAllNotes().first()
        val notes = (notesResponse as Success).data

        assertEquals(
            noteEntities.map { Note(it.noteId, it.title, it.note, it.created, it.isPinned) },
            notes
        )
    }

    @Test
    fun `deleteAllNotes should delete all notes in DAO`() = runTest {
        repository.deleteAllNotes()
        coVerify { notesDao.deleteAllNotes() }
    }
}
