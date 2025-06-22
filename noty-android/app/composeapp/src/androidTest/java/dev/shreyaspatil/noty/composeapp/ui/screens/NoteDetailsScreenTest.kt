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
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.testing.HiltAndroidTest
import dev.shreyaspatil.noty.composeapp.NotyScreenTest
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.view.viewmodel.NoteDetailViewModel
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID
import javax.inject.Inject

@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class NoteDetailsScreenTest : NotyScreenTest() {
    @LocalRepository
    @Inject
    lateinit var noteRepository: NotyNoteRepository

    private lateinit var noteId: String

    @Before
    fun setUp() {
        inject()
        noteId = "test-note-${System.currentTimeMillis()}-${UUID.randomUUID()}"
    }

    @Test
    fun navigateUp_onClickBackIcon() =
        runTest {
            var navigatingUp = false
            setNotyContent { NoteDetailScreen(onNavigateUp = { navigatingUp = true }) }

            onNodeWithContentDescription("Back").performClick()

            assertTrue(navigatingUp)
        }

    @Test
    fun hideSaveButton_onInvalidNoteContentInput() =
        runTest {
            setIdleAfter { prepopulateNote() }
            setNotyContent { NoteDetailScreen() }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"), timeoutMillis = 5000)

            // We only show save button when title as at least has 4 characters
            onNodeWithText("Lorem Ipsum").performTextClearance()
            waitForIdle()
            onNodeWithText("Title").performTextInput("Hi")

            waitForIdle()
            onNodeWithText("Hey there").performTextClearance()

            onNodeWithText("Save", useUnmergedTree = true).assertDoesNotExist()
        }

    @Test
    fun hideSaveButton_whenEditedContentIsSameAsPreviouslySavedContent() =
        runTest {
            setIdleAfter { prepopulateNote() }
            setNotyContent { NoteDetailScreen() }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"), timeoutMillis = 5000)

            onNodeWithText("Lorem Ipsum").performTextClearance()
            waitForIdle()

            onNodeWithText("Hey there").performTextClearance()
            waitForIdle()

            onNodeWithText("Title").performTextInput("Lorem Ipsum")
            onNodeWithText("Write note here").performTextInput("Hey there")

            onNodeWithText("Save", useUnmergedTree = true).assertDoesNotExist()
        }

    @Test
    fun showSaveButton_whenEditedContentIsNotSameAsPreviouslySavedContent() =
        runTest {
            setIdleAfter { prepopulateNote() }
            setNotyContent { NoteDetailScreen() }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"), timeoutMillis = 5000)

            onNodeWithText("Lorem Ipsum").performTextClearance()
            waitForIdle()

            onNodeWithText("Hey there").performTextClearance()
            waitForIdle()

            onNodeWithText("Title").performTextInput("Lorem Ipsum Edited")
            onNodeWithText("Write note here").performTextInput("Hey there, this is edited")

            waitForIdle()

            onNodeWithText("Save", useUnmergedTree = true).assertIsDisplayed()
        }

    @Test
    fun navigateUp_whenNoteIsUpdatedSuccessfully() =
        runTest {
            var navigatingUp = false

            setIdleAfter { prepopulateNote() }
            setNotyContent { NoteDetailScreen(onNavigateUp = { navigatingUp = true }) }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"), timeoutMillis = 5000)

            onNodeWithText("Lorem Ipsum").performTextInput("Hey there")
            waitForIdle()

            onNodeWithText("Hey there").performTextInput("Lorem Ipsum")
            waitForIdle()

            onNodeWithText("Save", useUnmergedTree = true).performClick()
            waitForIdle()

            assertTrue(navigatingUp)
        }

    @Test
    fun showActionToUnpinNote_whenNoteIsAlreadyPinned() =
        runTest {
            setIdleAfter { prepopulateNote(isPinned = true) }

            setNotyContent { NoteDetailScreen() }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"))
            waitUntil(5000) { onNodeWithContentDescription("Pinned").isDisplayed() }

            onNodeWithTag("actionTogglePin", useUnmergedTree = true)
                .assertContentDescriptionEquals("Pinned")
        }

    @Test
    fun showActionToPinNote_whenNoteIsNotPinned() =
        runTest {
            setIdleAfter { prepopulateNote(isPinned = false) }

            setNotyContent { NoteDetailScreen() }
            waitUntilAtLeastOneExists(hasText("Lorem Ipsum"))
            waitUntil(5000) { onNodeWithContentDescription("Not Pinned").isDisplayed() }

            onNodeWithTag("actionTogglePin", useUnmergedTree = true)
                .assertContentDescriptionEquals("Not Pinned")
        }

    @Composable
    private fun NoteDetailScreen(onNavigateUp: () -> Unit = {}) {
        NoteDetailsScreen(
            viewModel =
                hiltViewModel(creationCallback = { factory: NoteDetailViewModel.Factory ->
                    factory.create(noteId = noteId)
                }),
            onNavigateUp = onNavigateUp,
        )
    }

    private suspend fun prepopulateNote(isPinned: Boolean = false) {
        val note =
            Note(
                id = noteId,
                title = "Lorem Ipsum",
                note = "Hey there",
                created = System.currentTimeMillis(),
                isPinned = isPinned,
            )

        noteRepository.addNotes(listOf(note))
    }
}
