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
import dev.shreyaspatil.noty.fakes.note
import dev.shreyaspatil.noty.testUtils.currentStateShouldBe
import dev.shreyaspatil.noty.testUtils.withState
import dev.shreyaspatil.noty.view.state.NoteDetailState
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import java.util.*

class NoteDetailViewModelTest : ViewModelBehaviorSpec({
    val note = note("note-1234")
    val repository: NotyNoteRepository = mockk {
        coEvery { getNoteById("note-1234") } returns flowOf(note)
    }

    val scheduledTasks = mutableListOf<NotyTask>()

    val taskManager: NotyTaskManager = mockk {
        every { scheduleTask(capture(scheduledTasks)) } returns UUID.randomUUID()
    }

    val noteId = "note-1234"

    val viewModel = NoteDetailViewModel(taskManager, repository, noteId)

    Given("The ViewModel") {
        val expectedState = NoteDetailState(
            isLoading = false,
            title = "Lorem Ipsum",
            note = "Hey there! This is note content",
            showSave = false,
            finished = false,
            error = null
        )

        When("Initialized") {
            Then("Initial state should be valid") {
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

        And("Note contents are same as existing note contents") {
            val title = note.title
            val note = note.note

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
    }

    Given("A note for updating") {
        val title = "Lorem Ipsum"
        val note = "Updated body of a note"

        viewModel.setTitle(title)
        viewModel.setNote(note)

        And("Note is not yet synced") {
            coEvery { repository.updateNote(noteId, title, note) } returns Either.success(
                data = "TMP_$noteId"
            )

            When("Note is saved") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        finished shouldBe true
                    }
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

            When("Note is updated") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState {
                        isLoading shouldBe false
                        finished shouldBe true
                    }
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

            When("Note is updated") {
                viewModel.save()

                Then("Note should be get updated") {
                    coVerify { repository.updateNote(noteId, title, note) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { error shouldBe "Error occurred" }
                }
            }
        }
    }

    Given("A note for deletion") {
        And("Note is not yet synced") {
            coEvery { repository.deleteNote(noteId) } returns Either.success("TMP_$noteId")

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { finished shouldBe true }
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

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { finished shouldBe true }
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
            coEvery { repository.deleteNote(noteId) } returns Either.error("Error occurred")

            When("Note is deleted") {
                viewModel.delete()

                Then("Note should be get deleted") {
                    coVerify { repository.deleteNote(noteId) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { error shouldBe "Error occurred" }
                }
            }
        }
    }

    Given("A note is either pinned or unpinned") {
        And("Note is not yet synced") {
            val wasPinned = viewModel.currentState.isPinned
            coEvery { repository.pinNote(noteId, any()) } returns Either.success("TMP_$noteId")

            When("Note pin is toggled") {
                viewModel.togglePin()

                Then("Note should be get pinned") {
                    coVerify { repository.pinNote(noteId, !wasPinned) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { isPinned shouldBe !wasPinned }
                }

                Then("Note pin should NOT be get scheduled") {
                    scheduledTasks.find {
                        it.noteId == "TMP_$noteId" && it.action == NotyTaskAction.PIN
                    } shouldBe null
                }
            }
        }

        And("Note is synced") {
            val wasPinned = viewModel.currentState.isPinned
            coEvery { repository.pinNote(noteId, any()) } returns Either.success(noteId)

            When("Note pin is toggled") {
                viewModel.togglePin()

                Then("Note should get pinned") {
                    coVerify { repository.pinNote(noteId, !wasPinned) }
                }

                Then("Valid UI states should be get updated") {
                    viewModel.withState { isPinned shouldBe !wasPinned }
                }

                Then("Note pin should be get scheduled") {
                    scheduledTasks.last().let {
                        it.noteId shouldBe noteId
                        it.action shouldBe NotyTaskAction.PIN
                    }
                }
            }
        }

        And("Error occurs") {
            coEvery { repository.pinNote(noteId, any()) } returns Either.error("Error occurred")

            When("Note pin is toggled") {
                viewModel.togglePin()

                Then("Valid UI states should be get updated") {
                    viewModel.withState { error shouldBe "Error occurred" }
                }
            }
        }
    }
})
