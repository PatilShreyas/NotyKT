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
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.ui.UIDataState
import dev.shreyaspatil.noty.fakes.note
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest : BehaviorSpec({
    testCoroutineDispatcher = true
    Dispatchers.setMain(TestCoroutineDispatcher())

    val repository: NotyNoteRepository = mockk {
        coEvery { getNoteById("note-1234") } returns flowOf(note("note-1234"))
    }

    val scheduledTasks = mutableListOf<NotyTask>()

    val taskManager: NotyTaskManager = mockk {
        every { scheduleTask(capture(scheduledTasks)) } returns UUID.randomUUID()
    }

    val noteId = "note-1234"

    val viewModel = NoteDetailViewModel(taskManager, repository, noteId)

    Given("A note for updating") {
        val title = "Lorem Ipsum"
        val note = "Updated body of a note"

        And("Note is not yet synced") {
            coEvery { repository.updateNote(noteId, title, note) } returns Either.success(
                data = "TMP_$noteId"
            )

            val updateStates = mutableListOf<UIDataState<Unit>>()
            val collectUpdateStates = launch { viewModel.updateNoteState.toList(updateStates) }

            When("Note is updated") {
                viewModel.updateNote(title, note)

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get emitted") {
                    collectUpdateStates.cancel()
                    updateStates[0].isLoading shouldBe true
                    updateStates[1].isSuccess shouldBe true
                }

                Then("Note creation should be get scheduled") {
                    scheduledTasks.last().let {
                        it.noteId shouldBe "TMP_$noteId"
                        it.action shouldBe NotyTaskAction.CREATE
                    }
                }
            }
        }

        And("Note is synced") {
            coEvery { repository.updateNote(noteId, title, note) } returns Either.success(
                data = noteId
            )

            val updateStates = mutableListOf<UIDataState<Unit>>()
            val collectUpdateStates =
                launch { viewModel.updateNoteState.drop(1).toList(updateStates) }

            When("Note is updated") {
                viewModel.updateNote(title, note)

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get emitted") {
                    collectUpdateStates.cancel()
                    updateStates[0].isLoading shouldBe true
                    updateStates[1].isSuccess shouldBe true
                }

                Then("Note update should be get scheduled") {
                    scheduledTasks.last().let {
                        it.noteId shouldBe noteId
                        it.action shouldBe NotyTaskAction.UPDATE
                    }
                }
            }
        }

        And("Error occurs") {
            coEvery { repository.updateNote(noteId, title, note) } returns Either.error(
                message = "Error occurred"
            )

            val updateStates = mutableListOf<UIDataState<Unit>>()
            val collectUpdateStates =
                launch { viewModel.updateNoteState.drop(1).toList(updateStates) }

            When("Note is updated") {
                viewModel.updateNote(title, note)

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get emitted") {
                    collectUpdateStates.cancel()
                    updateStates[0].isLoading shouldBe true
                    updateStates[1].isFailed shouldBe true
                }
            }
        }
    }

    Given("A note for deletion") {
        And("Note is not yet synced") {
            coEvery { repository.deleteNote(noteId) } returns Either.success("TMP_$noteId")

            val deleteStates = mutableListOf<UIDataState<Unit>>()
            val collectDeleteStates = launch { viewModel.deleteNoteState.toList(deleteStates) }

            When("Note is deleted") {
                viewModel.deleteNote()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get emitted") {
                    collectDeleteStates.cancel()
                    deleteStates[0].isLoading shouldBe true
                    deleteStates[1].isSuccess shouldBe true
                }

                Then("Note deletion should NOT be get scheduled") {
                    scheduledTasks.find {
                        it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.DELETE
                    } shouldBe null
                }
            }
        }

        And("Note is synced") {
            coEvery { repository.deleteNote(noteId) } returns Either.success(noteId)

            val deleteStates = mutableListOf<UIDataState<Unit>>()
            val collectDeleteStates =
                launch { viewModel.deleteNoteState.drop(1).toList(deleteStates) }

            When("Note is deleted") {
                viewModel.deleteNote()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get emitted") {
                    collectDeleteStates.cancel()
                    deleteStates[0].isLoading shouldBe true
                    deleteStates[1].isSuccess shouldBe true
                }

                Then("Note deletion should be get scheduled") {
                    scheduledTasks.last().let {
                        it.noteId shouldBe noteId
                        it.action shouldBe NotyTaskAction.DELETE
                    }
                }
            }
        }

        And("Error occurs") {
            coEvery { repository.deleteNote(noteId) } returns Either.error("Error")

            val deleteStates = mutableListOf<UIDataState<Unit>>()
            val collectDeleteStates = launch {
                viewModel.deleteNoteState.drop(1).toList(deleteStates)
            }

            When("Note is deleted") {
                viewModel.deleteNote()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get emitted") {
                    collectDeleteStates.cancel()
                    deleteStates[0].isLoading shouldBe true
                    deleteStates[1].isFailed shouldBe true
                }
            }
        }
    }
})
