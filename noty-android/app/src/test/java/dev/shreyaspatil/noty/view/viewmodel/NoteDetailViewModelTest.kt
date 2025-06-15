package dev.shreyaspatil.noty.view.viewmodel

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.fakes.note
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.NoteDetailState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest : ViewModelBehaviorSpec() {

    private val note = note("note-1234")
    private lateinit var repository: NotyNoteRepository
    private lateinit var taskManager: NotyTaskManager
    private lateinit var viewModel: NoteDetailViewModel
    private val noteId = "note-1234"
    private val scheduledTasks = mutableListOf<NotyTask>()

    @BeforeEach
    override fun setUp() {
        super.setUp()
        scheduledTasks.clear()
        repository = mockk {
            coEvery { getNoteById(noteId) } returns flowOf(note)
        }
        taskManager = mockk {
            every { scheduleTask(capture(scheduledTasks)) } returns UUID.randomUUID()
        }
        viewModel = NoteDetailViewModel(taskManager, repository, noteId)
    }

    @Test
    fun `initial state should be valid`() {
        val expectedState = NoteDetailState(
            isLoading = false,
            title = "Lorem Ipsum",
            note = "Hey there! This is note content",
            showSave = false,
            finished = false,
            error = null,
            isPinned = false,
        )
        viewModel currentStateShouldBe expectedState
    }

    @Test
    fun `UI state should have validation details when note contents are invalid`() {
        val title = "hi"
        val noteContent = ""

        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        viewModel.withState {
            assertEquals(title, this.title)
            assertEquals(noteContent, this.note)
            assertFalse(showSave)
        }
    }

    @Test
    fun `UI state should have validation details when note contents are valid`() {
        val title = "Hey there"
        val noteContent = "This is body"

        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        viewModel.withState {
            assertEquals(title, this.title)
            assertEquals(noteContent, this.note)
            assertTrue(showSave)
        }
    }

    @Test
    fun `UI state should have validation details when note contents are same as existing`() {
        val title = note.title
        val noteContent = note.note

        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        viewModel.withState {
            assertEquals(title, this.title)
            assertEquals(noteContent, this.note)
            assertFalse(showSave)
        }
    }

    @Test
    fun `note should be updated and task scheduled when note is not yet synced`() {
        val title = "Lorem Ipsum"
        val noteContent = "Updated body of a note"
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.success("TMP_$noteId")

        viewModel.save()

        coVerify { repository.updateNote(noteId, title, noteContent) }
        viewModel.withState {
            assertFalse(isLoading)
            assertTrue(finished)
        }
        scheduledTasks.last().let {
            assertEquals("TMP_$noteId", it.noteId)
            assertEquals(NotyTaskAction.CREATE, it.action)
        }
    }

    @Test
    fun `note should be updated and task scheduled when note is synced`() {
        val title = "Lorem Ipsum"
        val noteContent = "Updated body of a note"
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.success(noteId)

        viewModel.save()

        coVerify { repository.updateNote(noteId, title, noteContent) }
        viewModel.withState {
            assertFalse(isLoading)
            assertTrue(finished)
        }
        scheduledTasks.last().let {
            assertEquals(noteId, it.noteId)
            assertEquals(NotyTaskAction.UPDATE, it.action)
        }
    }

    @Test
    fun `state should contain error when updating note fails`() {
        val title = "Lorem Ipsum"
        val noteContent = "Updated body of a note"
        viewModel.setTitle(title)
        viewModel.setNote(noteContent)

        coEvery { repository.updateNote(noteId, title, noteContent) } returns Either.error("Error occurred")

        viewModel.save()

        coVerify { repository.updateNote(noteId, title, noteContent) }
        viewModel.withState { assertEquals("Error occurred", error) }
    }

    @Test
    fun `note should be deleted and task not scheduled when note is not yet synced`() {
        coEvery { repository.deleteNote(noteId) } returns Either.success("TMP_$noteId")

        viewModel.delete()

        coVerify { repository.deleteNote(noteId) }
        viewModel.withState { assertTrue(finished) }
        assertTrue(scheduledTasks.none { it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.DELETE })
    }

    @Test
    fun `note should be deleted and task scheduled when note is synced`() {
        coEvery { repository.deleteNote(noteId) } returns Either.success(noteId)

        viewModel.delete()

        coVerify { repository.deleteNote(noteId) }
        viewModel.withState { assertTrue(finished) }
        scheduledTasks.last().let {
            assertEquals(noteId, it.noteId)
            assertEquals(NotyTaskAction.DELETE, it.action)
        }
    }

    @Test
    fun `state should contain error when deleting note fails`() {
        coEvery { repository.deleteNote(noteId) } returns Either.error("Error occurred")

        viewModel.delete()

        coVerify { repository.deleteNote(noteId) }
        viewModel.withState { assertEquals("Error occurred", error) }
    }

    @Test
    fun `note pin should be toggled and task not scheduled when note is not yet synced`() {
        val wasPinned = viewModel.currentState.isPinned
        coEvery { repository.pinNote(noteId, !wasPinned) } returns Either.success("TMP_$noteId")

        viewModel.togglePin()

        coVerify { repository.pinNote(noteId, !wasPinned) }
        viewModel.withState { assertEquals(!wasPinned, isPinned) }
        assertTrue(scheduledTasks.none { it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.PIN })
    }

    @Test
    fun `note pin should be toggled and task scheduled when note is synced`() {
        val wasPinned = viewModel.currentState.isPinned
        coEvery { repository.pinNote(noteId, !wasPinned) } returns Either.success(noteId)

        viewModel.togglePin()

        coVerify { repository.pinNote(noteId, !wasPinned) }
        viewModel.withState { assertEquals(!wasPinned, isPinned) }
        scheduledTasks.last().let {
            assertEquals(noteId, it.noteId)
            assertEquals(NotyTaskAction.PIN, it.action)
        }
    }

    @Test
    fun `state should contain error when toggling pin fails`() {
        val wasPinned = viewModel.currentState.isPinned
        coEvery { repository.pinNote(noteId, !wasPinned) } returns Either.error("Error occurred")

        viewModel.togglePin()

        viewModel.withState { assertEquals("Error occurred", error) }
    }
}
