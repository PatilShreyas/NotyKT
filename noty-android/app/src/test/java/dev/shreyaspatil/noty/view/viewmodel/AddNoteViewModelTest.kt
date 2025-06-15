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

import dev.shreyaspatil.noty.base.ViewModelTest
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.view.state.AddNoteState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class AddNoteViewModelTest : ViewModelTest() {
    private lateinit var repository: NotyNoteRepository
    private lateinit var taskManager: NotyTaskManager
    private lateinit var viewModel: AddNoteViewModel

    @BeforeEach
    fun setup() {
        repository = mockk(relaxUnitFun = true)
        taskManager =
            mockk(relaxUnitFun = true) {
                every { scheduleTask(any()) } returns UUID.randomUUID()
            }
        viewModel = AddNoteViewModel(repository, taskManager)
    }

    @Test
    fun `initial state should be valid`() {
        // Given
        val expectedState =
            AddNoteState(
                title = "",
                note = "",
                showSave = false,
                isAdding = false,
                added = false,
                errorMessage = null,
            )

        // Then
        assertEquals(expectedState, viewModel.currentState)
    }

    @Test
    fun `resetState should reset state to initial values`() {
        // Given
        viewModel.setTitle("Test Title")
        viewModel.setNote("Test Note")

        // When
        viewModel.resetState()

        // Then
        val expectedState =
            AddNoteState(
                title = "",
                note = "",
                showSave = false,
                isAdding = false,
                added = false,
                errorMessage = null,
            )
        assertEquals(expectedState, viewModel.currentState)
    }

    @Test
    fun `setTitle and setNote should update state with invalid content`() {
        // Given
        val title = "hi"
        val note = ""

        // When
        viewModel.setTitle(title)
        viewModel.setNote(note)

        // Then
        assertEquals(title, viewModel.currentState.title)
        assertEquals(note, viewModel.currentState.note)
        assertFalse(viewModel.currentState.showSave)
    }

    @Test
    fun `setTitle and setNote should update state with valid content`() {
        // Given
        val title = "Hey there"
        val note = "This is body"

        // When
        viewModel.setTitle(title)
        viewModel.setNote(note)

        // Then
        assertEquals(title, viewModel.currentState.title)
        assertEquals(note, viewModel.currentState.note)
        assertTrue(viewModel.currentState.showSave)
    }

    @Test
    fun `add should update state and schedule task when note addition is successful`() =
        runTest {
            // Given
            val title = "Lorem Ipsum"
            val note = "Hey there, this is not content"

            viewModel.setTitle(title)
            viewModel.setNote(note)
            coEvery { repository.addNote(title, note) } returns Either.success("note-11")

            // When
            viewModel.add()

            // Then
            assertFalse(viewModel.currentState.isAdding)
            assertTrue(viewModel.currentState.added)
            assertNull(viewModel.currentState.errorMessage)

            val actualTask = slot<NotyTask>()
            verify { taskManager.scheduleTask(capture(actualTask)) }

            assertEquals("note-11", actualTask.captured.noteId)
            assertEquals(NotyTaskAction.CREATE, actualTask.captured.action)
        }

    @Test
    fun `add should update state with error when note addition fails`() =
        runTest {
            // Given
            val title = "Lorem Ipsum"
            val note = "Hey there, this is not content"

            viewModel.setTitle(title)
            viewModel.setNote(note)
            clearAllMocks()
            coEvery { repository.addNote(title, note) } returns Either.error("Failed")

            // When
            viewModel.add()

            // Then
            assertFalse(viewModel.currentState.isAdding)
            assertFalse(viewModel.currentState.added)
            assertEquals("Failed", viewModel.currentState.errorMessage)

            verify(exactly = 0) { taskManager.scheduleTask(any()) }
        }
}
