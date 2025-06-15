/*
 * Copyright 2020 Shreyas Patil
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

class NotyLocalNoteRepositoryTest {
    private lateinit var notesDao: NotesDao
    private lateinit var repository: NotyLocalNoteRepository

    @BeforeEach
    fun setup() {
        notesDao = mockk(relaxUnitFun = true)
        repository = NotyLocalNoteRepository(notesDao)
    }

    @Test
    fun `addNote should return temporary note ID when DAO can add note`() =
        runTest {
            // Given
            val note =
                Note(
                    id = "UNIQUE_ID",
                    title = "Lorem Ipsum",
                    note = "This is body of a note!",
                    created = Date().time,
                    isPinned = false,
                )

            // When
            val response = repository.addNote(note.title, note.note)
            val noteId = (response as Success).data

            // Then
            assertTrue(noteId.startsWith("TMP"))

            val actualNoteEntity = slot<NoteEntity>()
            coVerify { notesDao.addNote(capture(actualNoteEntity)) }

            with(actualNoteEntity.captured) {
                assertEquals(note.title, this.title)
                assertEquals(note.note, this.note)
            }
        }

    @Test
    fun `addNote should return error when DAO cannot add note`() =
        runTest {
            // Given
            coEvery { notesDao.addNote(any()) } throws Exception("")

            // When
            val response = repository.addNote("Lorem Ipsum", "This is body of a note!")

            // Then
            assertEquals("Unable to create a new note", (response as Error).message)
        }

    @Test
    fun `addNotes should add notes in bulk to DAO`() =
        runTest {
            // Given
            val note =
                Note(
                    id = "UNIQUE_ID",
                    title = "Lorem Ipsum",
                    note = "This is body of a note!",
                    created = Date().time,
                    isPinned = false,
                )
            val expectedEntity =
                NoteEntity(note.id, note.title, note.note, note.created, note.isPinned)

            // When
            repository.addNotes(listOf(note))

            // Then
            coVerify { notesDao.addNotes(listOf(expectedEntity)) }
        }

    @Test
    fun `getNoteById should return note when observed`() =
        runTest {
            // Given
            val noteEntity =
                NoteEntity(
                    noteId = "UNIQUE_ID",
                    title = "Lorem Ipsum",
                    note = "This is body of a note!",
                    created = Date().time,
                    isPinned = false,
                )
            coEvery { notesDao.getNoteById(noteEntity.noteId) } returns flowOf(noteEntity)

            // When
            val actualNote = repository.getNoteById(noteEntity.noteId).first()

            // Then
            assertEquals(noteEntity.noteId, actualNote.id)
            assertEquals(noteEntity.title, actualNote.title)
            assertEquals(noteEntity.note, actualNote.note)
            assertEquals(noteEntity.created, actualNote.created)
        }

    @Test
    fun `updateNote should update note in DAO when DAO can update`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            val newTitle = "New title"
            val newNote = "New note body"
            coEvery { notesDao.updateNoteById(any(), any(), any()) } just Runs

            // When
            repository.updateNote(noteId, newTitle, newNote)

            // Then
            coVerify { notesDao.updateNoteById(noteId, newTitle, newNote) }
        }

    @Test
    fun `updateNote should return error when DAO cannot update`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            val newTitle = "New title"
            val newNote = "New note body"
            coEvery { notesDao.updateNoteById(any(), any(), any()) } throws Exception()

            // When
            val response = repository.updateNote(noteId, newTitle, newNote)

            // Then
            assertEquals("Unable to update a note", (response as Error).message)
        }

    @Test
    fun `updateNoteId should update note ID in DAO`() =
        runTest {
            // Given
            val oldNoteId = "OLD_NOTE_ID"
            val newNoteId = "NEW_NOTE_ID"

            // When
            repository.updateNoteId(oldNoteId = oldNoteId, newNoteId = newNoteId)

            // Then
            coVerify { notesDao.updateNoteId(oldNoteId, newNoteId) }
        }

    @Test
    fun `deleteNote should delete note in DAO when DAO can delete`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            coEvery { notesDao.deleteNoteById(any()) } just Runs

            // When
            repository.deleteNote(noteId)

            // Then
            coVerify { notesDao.deleteNoteById(noteId) }
        }

    @Test
    fun `deleteNote should return error when DAO cannot delete`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            coEvery { notesDao.deleteNoteById(any()) } throws Exception()

            // When
            val response = repository.deleteNote(noteId)

            // Then
            assertEquals("Unable to delete a note", (response as Error).message)
        }

    @Test
    fun `pinNote should pin note in DAO when DAO can pin`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            coEvery { notesDao.updateNotePin(any(), any()) } just Runs

            // When
            repository.pinNote(noteId, true)

            // Then
            coVerify { notesDao.updateNotePin(noteId, true) }
        }

    @Test
    fun `pinNote should return error when DAO cannot pin`() =
        runTest {
            // Given
            val noteId = "UNIQUE_ID"
            coEvery { notesDao.updateNotePin(any(), any()) } throws Exception()

            // When
            val response = repository.pinNote(noteId, false)

            // Then
            assertEquals("Unable to pin the note", (response as Error).message)
        }

    @Test
    fun `getAllNotes should retrieve all notes`() =
        runTest {
            // Given
            val note = NoteEntity("ID", "Title", "Note", 0, false)
            val noteEntities = listOf(note.copy(noteId = "1"), note.copy(noteId = "2"))
            coEvery { notesDao.getAllNotes() } returns flowOf(noteEntities)

            // When
            val notes = repository.getAllNotes().first()

            // Then
            val expectedNotes =
                noteEntities.map {
                    Note(it.noteId, it.title, it.note, it.created, it.isPinned)
                }
            assertEquals(expectedNotes, (notes as Success).data)
        }

    @Test
    fun `deleteAllNotes should delete all notes in DAO`() =
        runTest {
            // When
            repository.deleteAllNotes()

            // Then
            coVerify { notesDao.deleteAllNotes() }
        }
}
