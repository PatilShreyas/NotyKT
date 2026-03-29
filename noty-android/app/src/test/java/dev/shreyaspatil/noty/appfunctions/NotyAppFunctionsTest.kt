/*
 * Copyright 2026 Shreyas Patil
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

package dev.shreyaspatil.noty.appfunctions

import androidx.appfunctions.AppFunctionContext
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import dev.shreyaspatil.noty.core.model.Note as CoreNote

class NotyAppFunctionsTest {
    private lateinit var repository: NotyNoteRepository
    private lateinit var appFunctions: NotyAppFunctions
    private lateinit var mockContext: AppFunctionContext

    @BeforeEach
    fun setup() {
        repository = mockk(relaxUnitFun = true)
        mockContext = mockk(relaxUnitFun = true)
        appFunctions = NotyAppFunctions(repository)
    }

    @Test
    fun `listNotes should return mapped notes from repository`() =
        runTest {
            // Given
            val coreNotes =
                listOf(
                    CoreNote(id = "1", title = "Title 1", note = "Content 1", created = 1234),
                    CoreNote(id = "2", title = "Title 2", note = "Content 2", created = 5678),
                )
            coEvery { repository.getAllNotes() } returns flowOf(Either.success(coreNotes))

            // When
            val result = appFunctions.listNotes(mockContext)

            // Then
            assertEquals(2, result.size)
            assertEquals("1", result[0].id)
            assertEquals("Title 1", result[0].title)
            assertEquals("Content 1", result[0].content)
            assertEquals("2", result[1].id)
            assertEquals("Title 2", result[1].title)
            assertEquals("Content 2", result[1].content)
        }

    @Test
    fun `listNotes should return empty list when repository returns error`() =
        runTest {
            // Given
            coEvery { repository.getAllNotes() } returns flowOf(Either.error("Failed to load notes"))

            // When
            val result = appFunctions.listNotes(mockContext)

            // Then
            assertTrue(result.isEmpty())
        }

    @Test
    fun `createNote should return Note when repository successfully adds a note`() =
        runTest {
            // Given
            val title = "New Note"
            val content = "This is a new note"
            val expectedNoteId = "note-id-123"

            coEvery { repository.addNote(title, content) } returns Either.success(expectedNoteId)

            // When
            val result = appFunctions.createNote(mockContext, title, content)

            // Then
            assertEquals(expectedNoteId, result?.id)
            assertEquals(title, result?.title)
            assertEquals(content, result?.content)
        }

    @Test
    fun `createNote should return null when repository fails to add a note`() =
        runTest {
            // Given
            val title = "New Note"
            val content = "This is a new note"

            coEvery { repository.addNote(title, content) } returns Either.error("Failed")

            // When
            val result = appFunctions.createNote(mockContext, title, content)

            // Then
            assertNull(result)
        }

    @Test
    fun `editNote should return updated Note when repository updates note successfully`() =
        runTest {
            // Given
            val noteId = "1"
            val oldTitle = "Old Title"
            val oldContent = "Old Content"
            val newTitle = "New Title"
            val newContent = "New Content"

            val existingNote = CoreNote(id = noteId, title = oldTitle, note = oldContent, created = 1234)

            coEvery { repository.getNoteById(noteId) } returns flowOf(existingNote)
            coEvery { repository.updateNote(noteId, newTitle, newContent) } returns Either.success(noteId)

            // When
            val result = appFunctions.editNote(mockContext, noteId, newTitle, newContent)

            // Then
            assertEquals(noteId, result?.id)
            assertEquals(newTitle, result?.title)
            assertEquals(newContent, result?.content)
        }

    @Test
    fun `editNote should correctly handle partial updates (null title or null content)`() =
        runTest {
            // Given
            val noteId = "1"
            val existingNote =
                CoreNote(id = noteId, title = "Original Title", note = "Original Content", created = 1234)

            coEvery { repository.getNoteById(noteId) } returns flowOf(existingNote)
            coEvery { repository.updateNote(noteId, "Original Title", "Updated Content") } returns
                Either.success(noteId)

            // When
            // Supplying null for title should retain the original title
            val result = appFunctions.editNote(mockContext, noteId, title = null, content = "Updated Content")

            // Then
            assertEquals(noteId, result?.id)
            assertEquals("Original Title", result?.title)
            assertEquals("Updated Content", result?.content)
        }

    @Test
    fun `editNote should return null when repository fails to update the note`() =
        runTest {
            // Given
            val noteId = "1"
            val existingNote =
                CoreNote(id = noteId, title = "Original Title", note = "Original Content", created = 1234)

            coEvery { repository.getNoteById(noteId) } returns flowOf(existingNote)
            coEvery { repository.updateNote(any(), any(), any()) } returns Either.error("Error")

            // When
            val result = appFunctions.editNote(mockContext, noteId, "New Title", "New Content")

            // Then
            assertNull(result)
        }

    @Test
    fun `deleteNote should return true when repository successfully deletes a note`() =
        runTest {
            // Given
            val noteId = "1"
            coEvery { repository.deleteNote(noteId) } returns Either.success(noteId)

            // When
            val result = appFunctions.deleteNote(mockContext, noteId)

            // Then
            assertTrue(result)
        }

    @Test
    fun `deleteNote should return false when repository fails to delete a note`() =
        runTest {
            // Given
            val noteId = "1"
            coEvery { repository.deleteNote(noteId) } returns Either.error("Not found")

            // When
            val result = appFunctions.deleteNote(mockContext, noteId)

            // Then
            assertFalse(result)
        }
}
