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

package dev.shreyaspatil.noty.composeapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import dagger.hilt.android.testing.HiltAndroidTest
import dev.shreyaspatil.noty.composeapp.NotyScreenTest
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.di.LocalRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import javax.inject.Inject

@DelicateCoroutinesApi
@HiltAndroidTest
class NotesScreenTest : NotyScreenTest() {

    @LocalRepository
    @Inject
    lateinit var noteRepository: NotyNoteRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Before
    fun setUp() {
        inject()
        // Mock fake authentication
        sessionManager.saveToken("Bearer ABCD")
    }

    @Test
    fun navigateToLogin_whenUserNotExistsInSession() = runTest {
        // Remove user from session
        sessionManager.saveToken(null)

        var navigatingToLogin = false
        setNotyContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        waitForIdle()

        assertTrue(navigatingToLogin)
    }

    @Test
    fun navigateToAbout_onClickAboutIcon() = runTest {
        var navigatingToAbout = false
        setNotyContent {
            NotesScreen(onNavigateToAbout = { navigatingToAbout = true })
        }

        onNodeWithContentDescription("About").performClick()
        waitForIdle()
        assertTrue(navigatingToAbout)
    }

    @Test
    fun navigateToAddNote_onClickAddIcon() = runTest {
        var navigateToAddNote = false
        setNotyContent {
            NotesScreen(onNavigateToAddNote = { navigateToAddNote = true })
        }

        onNodeWithContentDescription("Add").performClick()
        waitForIdle()
        assertTrue(navigateToAddNote)
    }

    @Test
    fun navigateToLogin_onClickLogoutIconAndConfirmedLogin() = runTest {
        var navigatingToLogin = false
        setNotyContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        val logoutNode = onNodeWithContentDescription("Logout")
        logoutNode.performClick()

        // Should show confirmation dialog
        onNodeWithText("Logout?").assertIsDisplayed()
        onNodeWithText("Sure want to logout?").assertIsDisplayed()

        // Confirm logout
        onNodeWithText("Yes").performClick()
        waitForIdle()
        assertTrue(navigatingToLogin)
    }

    @Test
    fun shouldNotNavigateToLogin_onClickLogoutIconAndDeniedLogin() = runTest {
        var navigatingToLogin = false
        setNotyContent {
            NotesScreen(onNavigateToLogin = { navigatingToLogin = true })
        }

        val logoutNode = onNodeWithContentDescription("Logout")
        logoutNode.performClick()

        // Should show confirmation dialog
        onNodeWithText("Logout?").assertIsDisplayed()
        onNodeWithText("Sure want to logout?").assertIsDisplayed()

        // Confirm logout
        onNodeWithText("No").performClick()
        waitForIdle()
        assertFalse(navigatingToLogin)
    }

    @Test
    fun showNotes_whenNotesAreLoaded() = runTest {
        setNotyContent { NotesScreen() }
        registerIdlingResource(prefillNotes())

        waitForIdle()
        onNodeWithTag("notesList").performScrollToIndex(0)
        waitForIdle()

        val notes = notes()
        onNodeWithText(notes.first().title).assertExists()
        onNodeWithText(notes.first().note).assertExists()

        // Since it's LazyColumn, last note should not exist
        onNodeWithText(notes.last().title).assertDoesNotExist()
        onNodeWithText(notes.last().note).assertDoesNotExist()

        // Perform scrolling till end
        onNodeWithTag("notesList").performScrollToIndex(49)

        waitForIdle()

        // After scrolling, last item should be displayed
        onNodeWithText(notes.last().title).assertIsDisplayed()
        onNodeWithText(notes.last().note).assertIsDisplayed()
    }

    @Test
    fun showNoteOnRealtime_whenNewNoteIsAddedOrDeleted() = runTest {
        setNotyContent { NotesScreen() }

        registerIdlingResource(prefillNotes())
        registerIdlingResource(addNote(title = "New Note", note = "Hey there!"))

        waitForIdle()
        onNodeWithTag("notesList").performScrollToIndex(0)
        waitForIdle()

        // Newly added note should be displayed on UI
        onNodeWithText("New Note").assertIsDisplayed()
        onNodeWithText("Hey there!").assertIsDisplayed()

        registerIdlingResource(deleteNote("1"))

        // Deleted note should not exist
        onNodeWithText("Lorem Ipsum 1").assertDoesNotExist()
        onNodeWithText("Hello World 1").assertDoesNotExist()
    }

    @Test
    fun navigateToNoteDetail_onClickingNoteContent() = runTest {
        var navigateToNoteId: String? = null

        setNotyContent { NotesScreen(onNavigateToNoteDetail = { navigateToNoteId = it }) }
        registerIdlingResource(prefillNotes())

        onNodeWithText("Lorem Ipsum 2", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("2", navigateToNoteId)

        onNodeWithText("Hello World 3", useUnmergedTree = true).performClick()
        waitForIdle()
        assertEquals("3", navigateToNoteId)
    }

    @Test
    fun showPinnedNotesFirst_whenPinnedNotesArePresent() = runTest {
        setNotyContent { NotesScreen() }

        registerIdlingResource(prefillNotes())
        registerIdlingResource(pinNotes("49", "50"))

        waitForIdle()

        // Scroll to the top of screen
        onNodeWithTag("notesList").performScrollToIndex(0)
        waitForIdle()

        // Pinned notes should be displayed on top of screen
        onNodeWithText("Lorem Ipsum 49").assertIsDisplayed()
        onNodeWithText("Lorem Ipsum 50").assertIsDisplayed()
    }

    @Composable
    private fun NotesScreen(
        onNavigateToAbout: () -> Unit = {},
        onNavigateToAddNote: () -> Unit = {},
        onNavigateToNoteDetail: (String) -> Unit = {},
        onNavigateToLogin: () -> Unit = {}
    ) {
        NotesScreen(
            viewModel = viewModel(),
            onNavigateToAbout = onNavigateToAbout,
            onNavigateToAddNote = onNavigateToAddNote,
            onNavigateToNoteDetail = onNavigateToNoteDetail,
            onNavigateToLogin = onNavigateToLogin
        )
    }

    private fun notes(): List<Note> {
        val title = "Lorem Ipsum"
        val note = "Hello World"
        val currentTime = System.currentTimeMillis()

        return (1..50).map {
            Note(
                id = it.toString(),
                title = "$title $it",
                note = "$note $it",
                created = currentTime - it.toLong()
            )
        }
    }

    @After
    fun tearDown() = runBlocking { noteRepository.deleteAllNotes() }

    private fun prefillNotes() = addNotes(notes())

    private fun pinNotes(vararg noteIds: String): IdlingResource {
        return updateNotePins(noteIds.toList())
    }

    @Suppress("SameParameterValue")
    private fun addNote(title: String, note: String) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                noteRepository.addNote(title, note)
                delay(1_000)
                isIdleNow = true
            }
        }
    }

    private fun updateNotePins(noteIds: List<String>) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                noteIds.forEach { noteId ->
                    noteRepository.pinNote(noteId, true)
                }
                delay(1_000)
                isIdleNow = true
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun deleteNote(id: String) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                noteRepository.deleteNote(id)
                delay(1_000)
                isIdleNow = true
            }
        }
    }

    private fun addNotes(notes: List<Note>) = object : IdlingResource {
        override var isIdleNow: Boolean = false

        init {
            GlobalScope.launch {
                noteRepository.addNotes(notes)
                delay(1000)
                isIdleNow = true
            }
        }
    }
}
