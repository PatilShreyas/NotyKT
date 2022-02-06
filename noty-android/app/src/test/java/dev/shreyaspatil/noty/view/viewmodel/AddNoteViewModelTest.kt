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

import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.model.NotyTaskAction
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.ui.UIDataState
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class AddNoteViewModelTest : BehaviorSpec({
    testCoroutineDispatcher = true
    Dispatchers.setMain(TestCoroutineDispatcher())

    val repository: NotyNoteRepository = mockk(relaxUnitFun = true)
    val taskManager: NotyTaskManager = mockk(relaxUnitFun = true) {
        every { scheduleTask(any()) } returns UUID.randomUUID()
    }

    val viewModel = AddNoteViewModel(repository, taskManager)

    Given("A note for addition") {
        val title = "Lorem Ipsum"
        val note = "Hey there, this is not content"

        And("Note addition is successful") {
            coEvery { repository.addNote(title, note) } returns ResponseResult.success("note-11")
            val states = mutableListOf<UIDataState<String>>()
            val collectAddNoteStateJob = launch { viewModel.addNoteState.toList(states) }

            When("Note is added") {
                viewModel.addNote(title, note)

                Then("Note states should be valid") {
                    collectAddNoteStateJob.cancel()

                    states[0].isLoading shouldBe true
                    (states[1] as UIDataState.Success).data shouldBe "note-11"
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
            coEvery { repository.addNote(title, note) } returns ResponseResult.error("Failed")
            val states = mutableListOf<UIDataState<String>>()
            val collectAddNoteStateJob = launch { viewModel.addNoteState.toList(states) }

            When("Note is added") {
                viewModel.addNote(title, note)

                Then("Note states should be valid") {
                    collectAddNoteStateJob.cancel()

                    states[0].isLoading shouldBe true
                    (states[1] as UIDataState.Failed).message shouldBe "Failed"
                }
            }
        }
    }
})
