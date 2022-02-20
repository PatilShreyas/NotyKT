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

import dev.shreyaspatil.noty.base.ViewModelBehaviorSpec
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.AddNoteState
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest : ViewModelBehaviorSpec({

    val repository: NotyNoteRepository = mockk(relaxUnitFun = true)
    val taskManager: NotyTaskManager = mockk(relaxUnitFun = true) {
        every { scheduleTask(any()) } returns UUID.randomUUID()
    }

    val viewModel = AddNoteViewModel(repository, taskManager)

    Given("The ViewModel") {
        val expectedState = AddNoteState(
            title = "",
            note = "",
            showSave = false,
            isAdding = false,
            added = false,
            errorMessage = null
        )

        When("Initialized") {
            Then("Initial state should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }

        When("The state is reset") {
            viewModel.resetState()

            Then("State should be valid") {
                viewModel currentStateShouldBe expectedState
            }
        }
    }

    Given("Note contents") {
        And("Note contents are invalid") {
            val title = "hi"
            val note = ""

            When("When note contents are set") {
                viewModel.setTitle(title)
                viewModel.setNote(note)

                Then("UI state should have validation details") {
                    viewModel.withState {
                        this.title shouldBe title
                        this.note shouldBe note
                        showSave shouldBe false
                    }
                }
            }
        }

        And("Note contents are valid") {
            val title = "Hey there"
            val note = "This is body"

            When("When note contents are set") {
                viewModel.setTitle(title)
                viewModel.setNote(note)

                Then("UI state should have validation details") {
                    viewModel.withState {
                        this.title shouldBe title
                        this.note shouldBe note
                        showSave shouldBe true
                    }
                }
            }
        }
    }

    Given("A note for addition") {
        val title = "Lorem Ipsum"
        val note = "Hey there, this is not content"

        viewModel.setTitle(title)
        viewModel.setNote(note)

        And("Note addition is successful") {
            coEvery { repository.addNote(title, note) } returns Either.success("note-11")

            When("Note is added") {
                viewModel.add()

                Then("Note states should be valid") {
                    viewModel.withState {
                        isAdding shouldBe false
                        added shouldBe true
                        errorMessage shouldBe null
                    }
                }

                Then("Note creation task should be get scheduled") {
                    val actualTask = slot<NotyTask>()
                    verify { taskManager.scheduleTask(capture(actualTask)) }

                    actualTask.captured.let {
                        it.noteId shouldBe "note-11"
                        it.action shouldBe NotyTaskAction.CREATE
                    }
                }
            }
        }

        And("Note addition is failed") {
            clearAllMocks()

            coEvery { repository.addNote(title, note) } returns Either.error("Failed")

            When("Note is added") {
                viewModel.add()

                Then("Note states should be valid") {
                    viewModel.withState {
                        println("ThisStateIs: $this")
                        isAdding shouldBe false
                        added shouldBe false
                        errorMessage shouldBe "Failed"
                    }
                }

                Then("Note creation task should NOT be get scheduled") {
                    verify(exactly = 0) { taskManager.scheduleTask(any()) }
                }
            }
        }
    }
})
