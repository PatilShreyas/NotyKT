package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.AddNoteState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest : ViewModelBehaviorSpec() {

    private lateinit var repository: NotyNoteRepository
    private lateinit var taskManager: NotyTaskManager
    private lateinit var viewModel: AddNoteViewModel

    @BeforeEach
    override fun setUp() {
        super.setUp()
        repository = mockk(relaxUnitFun = true)
        taskManager = mockk(relaxUnitFun = true) {
            every { scheduleTask(any()) } returns UUID.randomUUID()
        }
        viewModel = AddNoteViewModel(repository, taskManager)
    }

    @Test
    fun `initial state should be valid`() {
        val expectedState = AddNoteState(
            title = "",
            note = "",
            showSave = false,
            isAdding = false,
            added = false,
            errorMessage = null,
        )
        viewModel currentStateShouldBe expectedState
    }

    @Test
    fun `state should be valid when reset`() {
        val expectedState = AddNoteState(
            title = "",
            note = "",
            showSave = false,
            isAdding = false,
            added = false,
            errorMessage = null,
        )
        viewModel.resetState()
        viewModel currentStateShouldBe expectedState
    }

    @Test
    fun `UI state should have validation details when note contents are invalid`() {
        val title = "hi"
        val note = ""

        viewModel.setTitle(title)
        viewModel.setNote(note)

        viewModel.withState {
            assertEquals(title, this.title)
            assertEquals(note, this.note)
            assertFalse(showSave)
        }
    }

    @Test
    fun `UI state should have validation details when note contents are valid`() {
        val title = "Hey there"
        val note = "This is body"

        viewModel.setTitle(title)
        viewModel.setNote(note)

        viewModel.withState {
            assertEquals(title, this.title)
            assertEquals(note, this.note)
            assertTrue(showSave)
        }
    }

    @Test
    fun `note states should be valid and task scheduled when note addition is successful`() {
        val title = "Lorem Ipsum"
        val note = "Hey there, this is not content"
        viewModel.setTitle(title)
        viewModel.setNote(note)

        coEvery { repository.addNote(title, note) } returns Either.success("note-11")

        viewModel.add()

        viewModel.withState {
            assertFalse(isAdding)
            assertTrue(added)
            assertNull(errorMessage)
        }

        val actualTask = slot<NotyTask>()
        verify { taskManager.scheduleTask(capture(actualTask)) }

        actualTask.captured.let {
            assertEquals("note-11", it.noteId)
            assertEquals(NotyTaskAction.CREATE, it.action)
        }
    }

    @Test
    fun `note states should be valid and task not scheduled when note addition is failed`() {
        val title = "Lorem Ipsum"
        val note = "Hey there, this is not content"
        viewModel.setTitle(title)
        viewModel.setNote(note)

        coEvery { repository.addNote(title, note) } returns Either.error("Failed")

        viewModel.add()

        viewModel.withState {
            assertFalse(isAdding)
            assertFalse(added)
            assertEquals("Failed", errorMessage)
        }

        verify(exactly = 0) { taskManager.scheduleTask(any()) }
    }
}
