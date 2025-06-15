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

package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.fakes.note
import dev.shreyaspatil.noty.view.state.NoteDetailState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class NoteDetailViewModelTest : ViewModelTest() {
    private lateinit var note: dev.shreyaspatil.noty.core.model.Note
    private lateinit var repository: NotyNoteRepository
    private lateinit var taskManager: NotyTaskManager
    private lateinit var viewModel: NoteDetailViewModel
    private lateinit var noteId: String
    private val scheduledTasks = mutableListOf<NotyTask>()

    @BeforeEach
    fun setup() {
        noteId = "note-1234"
        note = note(noteId)

        repository =
            mockk {
                coEvery { getNoteById(noteId) } returns flowOf(note)
            }

        taskManager =
            mockk {
                every { scheduleTask(capture(scheduledTasks)) } returns UUID.randomUUID()
            }

        viewModel = NoteDetailViewModel(taskManager, repository, noteId)
    }

    @Test
    fun `initial state should be valid`() {
        // Given
        val expectedState =
            NoteDetailState(
                isLoading = false,
                title = "Lorem Ipsum",
                note = "Hey there! This is note content",
                showSave = false,
                finished = false,
                error = null,
                isPinned = false,
            )

        // Then
        assertEquals(expectedState, viewModel.currentState)
    }

    @Test
    fun `setTitle and setNote should update state with invalid content`() {
        // Given
        val title = "hi"
        val noteContent = ""

        // When
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        // Then
        assertEquals(title, viewModel.currentState.title)
        assertEquals(noteContent, viewModel.currentState.note)
        assertFalse(viewModel.currentState.showSave)
    }

    @Test
    fun `setTitle and setNote should update state with valid content`() {
        // Given
        val title = "Hey there"
        val noteContent = "This is body"

        // When
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        // Then
        assertEquals(title, viewModel.currentState.title)
        assertEquals(noteContent, viewModel.currentState.note)
        assertTrue(viewModel.currentState.showSave)
    }

    @Test
    fun `setTitle and setNote should not show save button when content is same as existing note`() {
        // Given
        val title = note.title
        val noteContent = note.note

        // When
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        // Then
        assertEquals(title, viewModel.currentState.title)
        assertEquals(noteContent, viewModel.currentState.note)
        assertFalse(viewModel.currentState.showSave)
    }

    @Test
    fun `save should update note and schedule task when note is not yet synced`() =
        runTest {
            // Given
            val title = "Lorem Ipsum"
            val noteContent = "Updated body of a note"

            viewModel.setTitle(title)
            viewModel.setNote(noteContent)

            coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.success("TMP_$noteId")

            // When
            viewModel.save()

            // Then
            coVerify { repository.updateNote(noteId, title, noteContent) }
            assertFalse(viewModel.currentState.isLoading)
            assertTrue(viewModel.currentState.finished)

            val lastTask = scheduledTasks.last()
            assertEquals("TMP_$noteId", lastTask.noteId)
            assertEquals(NotyTaskAction.CREATE, lastTask.action)
        }

    @Test
    fun `save should update note and schedule task when note is synced`() =
        runTest {
            // Given
            val title = "Lorem Ipsum"
            val noteContent = "Updated body of a note"

            viewModel.setTitle(title)
            viewModel.setNote(noteContent)

            coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.success(noteId)

            // When
            viewModel.save()

            // Then
            coVerify { repository.updateNote(noteId, title, noteContent) }
            assertFalse(viewModel.currentState.isLoading)
            assertTrue(viewModel.currentState.finished)

            val lastTask = scheduledTasks.last()
            assertEquals(noteId, lastTask.noteId)
            assertEquals(NotyTaskAction.UPDATE, lastTask.action)
        }

    @Test
    fun `save should update state with error when update fails`() =
        runTest {
            // Given
            val title = "Lorem Ipsum"
            val noteContent = "Updated body of a note"

            viewModel.setTitle(title)
            viewModel.setNote(noteContent)

            coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.error("Error occurred")

            // When
            viewModel.save()

            // Then
            coVerify { repository.updateNote(noteId, title, noteContent) }
            assertEquals("Error occurred", viewModel.currentState.error)
        }

    @Test
    fun `delete should delete note without scheduling task when note is not yet synced`() =
        runTest {
            // Given
            coEvery { repository.deleteNote(noteId) } returns Either.success("TMP_$noteId")

            // When
            viewModel.delete()

            // Then
            coVerify { repository.deleteNote(noteId) }
            assertTrue(viewModel.currentState.finished)

            // No task should be scheduled for deletion of unsynced note
            val deleteTask =
                scheduledTasks.find {
                    it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.DELETE
                }
            assertNull(deleteTask)
        }

    @Test
    fun `delete should delete note and schedule task when note is synced`() =
        runTest {
            // Given
            coEvery { repository.deleteNote(noteId) } returns Either.success(noteId)

            // When
            viewModel.delete()

            // Then
            coVerify { repository.deleteNote(noteId) }
            assertTrue(viewModel.currentState.finished)

            val lastTask = scheduledTasks.last()
            assertEquals(noteId, lastTask.noteId)
            assertEquals(NotyTaskAction.DELETE, lastTask.action)
        }

    @Test
    fun `delete should update state with error when deletion fails`() =
        runTest {
            // Given
            coEvery { repository.deleteNote(noteId) } returns Either.error("Error occurred")

            // When
            viewModel.delete()

            // Then
            coVerify { repository.deleteNote(noteId) }
            assertEquals("Error occurred", viewModel.currentState.error)
        }

    @Test
    fun `togglePin should update pin status without scheduling task when note is not yet synced`() =
        runTest {
            // Given
            val wasPinned = viewModel.currentState.isPinned
            coEvery { repository.pinNote(noteId, any()) } returns Either.success("TMP_$noteId")

            // When
            viewModel.togglePin()

            // Then
            coVerify { repository.pinNote(noteId, !wasPinned) }
            assertEquals(!wasPinned, viewModel.currentState.isPinned)

            // No task should be scheduled for pin toggle of unsynced note
            val pinTask =
                scheduledTasks.find {
                    it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.PIN
                }
            assertNull(pinTask)
        }

    @Test
    fun `togglePin should update pin status and schedule task when note is synced`() =
        runTest {
            // Given
            val wasPinned = viewModel.currentState.isPinned
            coEvery { repository.pinNote(noteId, any()) } returns Either.success(noteId)

            // When
            viewModel.togglePin()

            // Then
            coVerify { repository.pinNote(noteId, !wasPinned) }
            assertEquals(!wasPinned, viewModel.currentState.isPinned)

            val lastTask = scheduledTasks.last()
            assertEquals(noteId, lastTask.noteId)
            assertEquals(NotyTaskAction.PIN, lastTask.action)
        }

    @Test
    fun `togglePin should update state with error when pin toggle fails`() =
        runTest {
            // Given
            coEvery { repository.pinNote(noteId, any()) } returns Either.error("Error occurred")

            // When
            viewModel.togglePin()

            // Then
            assertEquals("Error occurred", viewModel.currentState.error)
        }
}
