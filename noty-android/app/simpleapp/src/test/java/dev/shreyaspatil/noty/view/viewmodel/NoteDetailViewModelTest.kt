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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.testutil.CoroutinesTestRule
import dev.shreyaspatil.noty.testutil.MockFailureNotyNoteRepository
import dev.shreyaspatil.noty.testutil.MockSuccessNotyNoteRepository
import dev.shreyaspatil.noty.testutil.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class NoteDetailViewModelTest {

    private lateinit var viewModel: NoteDetailViewModel

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        viewModel = NoteDetailViewModel(MockSuccessNotyNoteRepository(), "noteId")
        assertEquals(
            Note(
                id = "fake_id_1",
                title = "fake_title_1",
                note = "fake_note_1",
                created = 1000
            ), viewModel.noteLiveData.getOrAwaitValue()
        )
    }

    @Test
    fun updateNotes_withValidData_shouldReturnSuccess() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            viewModel.updateNoteState.observeForever { state ->
                when (state) {
                    is ViewState.Success -> assertEquals(Unit, state.data)
                    is ViewState.Failed -> fail()
                }
            }
            viewModel.updateNote("title", "note")
        }

    @Test
    fun updateNotes_whenUpdatesFails_shouldReturnFailure() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            //Initialising view model with fake failure repository
            viewModel = NoteDetailViewModel(MockFailureNotyNoteRepository(), "noteId")
            viewModel.updateNoteState.observeForever { state ->
                when (state) {
                    is ViewState.Success -> fail()
                    is ViewState.Failed -> assertEquals("Error", state.message)
                }
            }
            viewModel.updateNote("title", "note")
        }

    @Test
    fun deleteNotes_whenDeleteFails_shouldReturnFailure() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            //Initialising view model with fake failure repository
            viewModel = NoteDetailViewModel(MockFailureNotyNoteRepository(), "noteId")
            viewModel.updateNoteState.observeForever { state ->
                when (state) {
                    is ViewState.Success -> fail()
                    is ViewState.Failed -> assertEquals("Error", state.message)
                }
            }
            viewModel.deleteNote()
        }

    @Test
    fun deleteNotes_withValidData_shouldReturnSuccess() =
        coroutinesTestRule.testDispatcher.runBlockingTest {
            viewModel.updateNoteState.observeForever { state ->
                when (state) {
                    is ViewState.Success -> assertEquals(Unit, state.data)
                    is ViewState.Failed -> fail()
                }
            }
            viewModel.deleteNote()
        }
}
