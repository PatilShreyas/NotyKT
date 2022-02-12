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
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.core.ui.UIDataState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest : BehaviorSpec({
    testCoroutineDispatcher = true
    Dispatchers.setMain(TestCoroutineDispatcher())

    val repository: NotyNoteRepository = mockk(relaxUnitFun = true) {
        every { getAllNotes() } returns flowOf(
            Either.success(listOf(Note("NOTE_ID", "Lorem Ipsum", "Note text", 0))),
            Either.error("Failed to retrieve notes")
        )
    }
    val sessionManager: SessionManager = mockk(relaxUnitFun = true) {
        every { getToken() } returns "TEST_TOKEN"
    }
    val preferenceManager: PreferenceManager = mockk(relaxUnitFun = true)
    val taskManager: NotyTaskManager = mockk(relaxUnitFun = true)

    val viewModel = NotesViewModel(
        repository,
        sessionManager,
        preferenceManager,
        taskManager
    )

    Given("Notes from repository") {
        val notesState = mutableListOf<UIDataState<List<Note>>>()
        When("Notes are observed") {
            val collectNotesStateJob = launch { viewModel.notes.toList(notesState) }

            Then("Note states should be valid") {
                collectNotesStateJob.cancel()

                notesState[0].isLoading shouldBe true
                (notesState[1] as UIDataState.Success).data shouldHaveSize 1
                (notesState[2] as UIDataState.Failed).message shouldBe "Failed to retrieve notes"
            }
        }
    }

    Given("Notes available for syncing") {

        When("Sync for notes is requested") {

            And("Sync is successful") {
                val taskId = UUID.randomUUID()
                every { taskManager.syncNotes() } returns taskId
                every { taskManager.observeTask(taskId) } returns flowOf(
                    TaskState.SCHEDULED,
                    TaskState.COMPLETED
                )

                val states = mutableListOf<UIDataState<Unit>>()
                val collectStatesJob = launch { viewModel.syncState.toList(states) }

                viewModel.syncNotes()

                Then("UI state should be get updated") {
                    collectStatesJob.cancel()

                    // Loading should be the first state
                    states[0].isLoading shouldBe true

                    // Success should be the second state
                    states[1].isSuccess shouldBe true
                }
            }

            And("Sync is failed") {
                val taskId = UUID.randomUUID()
                every { taskManager.syncNotes() } returns taskId
                every { taskManager.observeTask(taskId) } returns flowOf(
                    TaskState.SCHEDULED,
                    TaskState.FAILED
                )

                val states = mutableListOf<UIDataState<Unit>>()

                // Drop [1] emission because it's replay of previous state
                val collectStatesJob = launch { viewModel.syncState.drop(1).toList(states) }

                viewModel.syncNotes()

                Then("UI state should be get updated") {
                    collectStatesJob.cancel()

                    // Loading should be the first state
                    states[0].isLoading shouldBe true

                    // Success should be the second state
                    states[1].isFailed shouldBe true
                }
            }
        }
    }

    Given("A UI mode") {
        val expectedUiMode = true

        coEvery { preferenceManager.uiModeFlow } returns flowOf(expectedUiMode)

        When("UI mode is changed") {
            viewModel.setDarkMode(expectedUiMode)

            Then("Preference should be get saved") {
                coVerify { preferenceManager.setDarkMode(expectedUiMode) }
            }
        }

        When("Current UI mode is retrieved") {
            val actualUiMode = viewModel.isDarkModeEnabled()

            Then("Correct UI mode should be get returned") {
                actualUiMode shouldBe expectedUiMode
            }
        }
    }

    Given("User session") {
        When("Session is cleared") {
            viewModel.clearUserSession()

            Then("Token should get reset") {
                verify { sessionManager.saveToken(null) }
            }

            Then("All scheduled tasks should be get aborted") {
                verify { taskManager.abortAllTasks() }
            }

            Then("All notes should be get cleared") {
                coVerify { repository.deleteAllNotes() }
            }

            Then("User logged in state should be changed to not logged in") {
                viewModel.userLoggedInState.value shouldBe false
            }
        }
    }
})
