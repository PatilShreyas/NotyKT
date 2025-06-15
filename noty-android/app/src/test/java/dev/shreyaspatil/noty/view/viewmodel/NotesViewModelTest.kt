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
import dev.shreyaspatil.noty.core.connectivity.ConnectionState
import dev.shreyaspatil.noty.core.connectivity.ConnectivityObserver
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.fakes.note
import dev.shreyaspatil.noty.view.state.NotesState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
class NotesViewModelTest : ViewModelTest() {
    private lateinit var fakeNotesFlow: MutableSharedFlow<Either<List<Note>>>
    private lateinit var repository: NotyNoteRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var defaultTaskId: UUID
    private lateinit var taskManager: NotyTaskManager
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private lateinit var viewModel: NotesViewModel

    @BeforeEach
    fun setup() {
        fakeNotesFlow = MutableSharedFlow(replay = 1)

        repository =
            mockk(relaxUnitFun = true) {
                every { getAllNotes() } returns fakeNotesFlow
            }

        sessionManager =
            mockk(relaxUnitFun = true) {
                every { getToken() } returns "TEST_TOKEN"
            }

        preferenceManager = mockk(relaxUnitFun = true)

        defaultTaskId = UUID.randomUUID()

        taskManager =
            mockk(relaxUnitFun = true) {
                every { syncNotes() } returns defaultTaskId
                every { observeTask(defaultTaskId) } returns
                    flowOf(
                        TaskState.SCHEDULED,
                        TaskState.COMPLETED
                    )
            }

        connectivityObserver = spyk(FakeConnectivityObserver())

        viewModel =
            NotesViewModel(
                notyNoteRepository = repository,
                sessionManager = sessionManager,
                preferenceManager = preferenceManager,
                notyTaskManager = taskManager,
                connectivityObserver = connectivityObserver
            )
    }

    @Test
    fun `initial state should be valid with notes loaded`() = runTest {
        // Given
        val initialNotes = listOf(Note("NOTE_ID", "Lorem Ipsum", "Note text", 0))
        fakeNotesFlow.emit(Either.success(initialNotes))

        // Then
        val expectedState =
            NotesState(
                isLoading = false,
                notes = initialNotes,
                error = null,
                isUserLoggedIn = true,
                isConnectivityAvailable = true
            )

        assertEquals(expectedState, viewModel.currentState)
        verify { sessionManager.getToken() }
        verify { taskManager.syncNotes() }
    }

    @Test
    fun `state should update when notes are successfully loaded`() = runTest {
        // Given
        val notes = listOf(note("1"), note("2"), note("3"))

        // When
        fakeNotesFlow.emit(Either.success(notes))

        // Then
        assertEquals(notes, viewModel.currentState.notes)
        assertFalse(viewModel.currentState.isLoading)
    }

    @Test
    fun `state should update with error when notes loading fails`() = runTest {
        // When
        fakeNotesFlow.emit(Either.error("Error occurred"))

        // Then
        assertEquals("Error occurred", viewModel.currentState.error)
        assertFalse(viewModel.currentState.isLoading)
    }

    @Test
    fun `state should update when connectivity is available`() {
        // When
        connectivityObserver.fakeConnectionFlow.value = ConnectionState.Available

        // Then
        assertTrue(viewModel.currentState.isConnectivityAvailable!!)
    }

    @Test
    fun `state should update when connectivity is unavailable`() {
        // When
        connectivityObserver.fakeConnectionFlow.value = ConnectionState.Unavailable

        // Then
        assertFalse(viewModel.currentState.isConnectivityAvailable!!)
    }

    @Test
    fun `syncNotes should not schedule task when user is not logged in`() {
        // Given
        clearAllMocks(answers = false)
        every { sessionManager.getToken() } returns null

        // When
        viewModel.syncNotes()

        // Then
        verify(exactly = 0) { taskManager.syncNotes() }
    }

    @Test
    fun `syncNotes should schedule task and update state when sync is successful`() = runTest {
        // Given
        clearAllMocks(answers = false)
        every { sessionManager.getToken() } returns "ABCD1234"

        val taskId = UUID.randomUUID()
        every { taskManager.syncNotes() } returns taskId
        every { taskManager.observeTask(taskId) } returns
            flowOf(
                TaskState.SCHEDULED,
                TaskState.COMPLETED
            )

        // When
        viewModel.syncNotes()

        // Then
        verify(exactly = 1) { taskManager.syncNotes() }
        assertFalse(viewModel.currentState.isLoading)
    }

    @Test
    fun `syncNotes should update state with error when sync fails`() = runTest {
        // Given
        clearAllMocks(answers = false)
        every { sessionManager.getToken() } returns "ABCD1234"

        val taskId = UUID.randomUUID()
        every { taskManager.syncNotes() } returns taskId
        every { taskManager.observeTask(taskId) } returns
            flowOf(
                TaskState.SCHEDULED,
                TaskState.FAILED
            )

        // When
        viewModel.syncNotes()

        // Then
        assertFalse(viewModel.currentState.isLoading)
        assertEquals("Failed to sync notes", viewModel.currentState.error)
    }

    @Test
    fun `setDarkMode should save preference`() {
        // Given
        val expectedUiMode = true

        // When
        viewModel.setDarkMode(expectedUiMode)

        // Then
        coVerify { preferenceManager.setDarkMode(expectedUiMode) }
    }

    @Test
    fun `isDarkModeEnabled should return correct UI mode`() = runTest {
        // Given
        val expectedUiMode = true
        coEvery { preferenceManager.uiModeFlow } returns flowOf(expectedUiMode)

        // When
        val actualUiMode = viewModel.isDarkModeEnabled()

        // Then
        assertEquals(expectedUiMode, actualUiMode)
    }

    @Test
    fun `logout should clear session, abort tasks, delete notes, and update state`() {
        // When
        viewModel.logout()

        // Then
        verify { sessionManager.saveToken(null) }
        verify { taskManager.abortAllTasks() }
        coVerify { repository.deleteAllNotes() }
        assertFalse(viewModel.currentState.isUserLoggedIn!!)
    }
}

class FakeConnectivityObserver : ConnectivityObserver {
    val fakeConnectionFlow = MutableStateFlow<ConnectionState>(ConnectionState.Available)

    override val connectionState: Flow<ConnectionState> = fakeConnectionFlow
    override var currentConnectionState: ConnectionState = ConnectionState.Available
}
