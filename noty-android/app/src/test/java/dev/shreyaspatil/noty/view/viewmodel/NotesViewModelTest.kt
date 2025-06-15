package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
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
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.NotesState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest : ViewModelBehaviorSpec() {

    private lateinit var fakeNotesFlow: MutableSharedFlow<Either<List<Note>>>
    private lateinit var repository: NotyNoteRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var taskManager: NotyTaskManager
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private lateinit var viewModel: NotesViewModel
    private val defaultTaskId = UUID.randomUUID()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        fakeNotesFlow = MutableSharedFlow(replay = 1)
        repository = mockk(relaxUnitFun = true) {
            every { getAllNotes() } returns fakeNotesFlow
        }
        sessionManager = mockk(relaxUnitFun = true) {
            every { getToken() } returns "TEST_TOKEN"
        }
        preferenceManager = mockk(relaxUnitFun = true)
        taskManager = mockk(relaxUnitFun = true) {
            every { syncNotes() } returns defaultTaskId
            every { observeTask(defaultTaskId) } returns flowOf(TaskState.SCHEDULED, TaskState.COMPLETED)
        }
        connectivityObserver = spyk(FakeConnectivityObserver())
        viewModel = NotesViewModel(
            notyNoteRepository = repository,
            sessionManager = sessionManager,
            preferenceManager = preferenceManager,
            notyTaskManager = taskManager,
            connectivityObserver = connectivityObserver,
        )
    }

    @Test
    fun `initial state should be valid, session checked and notes synced`() = runTest {
        val initialNotes = listOf(Note("NOTE_ID", "Lorem Ipsum", "Note text", 0))
        fakeNotesFlow.emit(Either.success(initialNotes))

        val expectedState = NotesState(
            isLoading = false,
            notes = initialNotes,
            error = null,
            isUserLoggedIn = true,
            isConnectivityAvailable = true,
        )

        viewModel currentStateShouldBe expectedState
        verify { sessionManager.getToken() }
        verify { taskManager.syncNotes() }
    }

    @Test
    fun `notes should be updated in state when notes updated successfully`() = runTest {
        val notes = listOf(note("1"), note("2"), note("3"))
        fakeNotesFlow.emit(Either.success(notes))
        viewModel.withState {
            assertEquals(notes, this.notes)
            assertFalse(isLoading)
        }
    }

    @Test
    fun `error should be updated in state when notes updated with failure`() = runTest {
        fakeNotesFlow.emit(Either.error("Error occurred"))
        viewModel.withState {
            assertEquals("Error occurred", error)
            assertFalse(isLoading)
        }
    }

    @Test
    fun `connectivity state should be updated when connectivity is available`() {
        connectivityObserver.fakeConnectionFlow.value = ConnectionState.Available
        viewModel.withState { assertTrue(isConnectivityAvailable!!) }
    }

    @Test
    fun `connectivity state should be updated when connectivity is unavailable`() {
        connectivityObserver.fakeConnectionFlow.value = ConnectionState.Unavailable
        viewModel.withState { assertFalse(isConnectivityAvailable!!) }
    }

    @Test
    fun `task should not be scheduled when user is not logged in and sync requested`() {
        every { sessionManager.getToken() } returns null
        viewModel.syncNotes()
        verify(exactly = 0) { taskManager.syncNotes() }
    }

    @Test
    fun `task should be scheduled and UI state updated when user is logged in and sync is successful`() {
        every { sessionManager.getToken() } returns "ABCD1234"
        val taskId = UUID.randomUUID()
        every { taskManager.syncNotes() } returns taskId
        every { taskManager.observeTask(taskId) } returns flowOf(TaskState.SCHEDULED, TaskState.COMPLETED)

        viewModel.syncNotes()

        verify(exactly = 1) { taskManager.syncNotes() } // This will be 2 if counting the initial one
        viewModel.withState { assertFalse(isLoading) }
    }

    @Test
    fun `UI state should be updated with error when user is logged in and sync failed`() {
        every { sessionManager.getToken() } returns "ABCD1234"
        val taskId = UUID.randomUUID()
        every { taskManager.syncNotes() } returns taskId
        every { taskManager.observeTask(taskId) } returns flowOf(TaskState.SCHEDULED, TaskState.FAILED)

        viewModel.syncNotes()
        viewModel.withState {
            assertFalse(isLoading)
            assertEquals("Failed to sync notes", error)
        }
    }

    @Test
    fun `preference should be saved when UI mode is changed`() = runTest {
        val expectedUiMode = true
        viewModel.setDarkMode(expectedUiMode)
        coVerify { preferenceManager.setDarkMode(expectedUiMode) }
    }

    @Test
    fun `correct UI mode should be returned when current UI mode is retrieved`() = runTest {
        val expectedUiMode = true
        coEvery { preferenceManager.uiModeFlow } returns flowOf(expectedUiMode)
        val actualUiMode = viewModel.isDarkModeEnabled()
        assertEquals(expectedUiMode, actualUiMode)
    }

    @Test
    fun `session cleared, tasks aborted, notes cleared and logged out state updated when logout`() = runTest {
        viewModel.logout()
        verify { sessionManager.saveToken(null) }
        verify { taskManager.abortAllTasks() }
        coVerify { repository.deleteAllNotes() }
        viewModel.withState { assertFalse(isUserLoggedIn) }
    }
}

class FakeConnectivityObserver : ConnectivityObserver {
    val fakeConnectionFlow = MutableStateFlow<ConnectionState>(ConnectionState.Available)
    override val connectionState: Flow<ConnectionState> = fakeConnectionFlow
    override var currentConnectionState: ConnectionState = ConnectionState.Available
}
