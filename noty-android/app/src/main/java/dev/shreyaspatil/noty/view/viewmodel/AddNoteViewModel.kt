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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.store.StateStore
import dev.shreyaspatil.noty.utils.validator.NoteValidator
import dev.shreyaspatil.noty.view.state.AddNoteState
import dev.shreyaspatil.noty.view.state.MutableAddNoteState
import dev.shreyaspatil.noty.view.state.mutable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel
    @Inject
    constructor(
        @LocalRepository private val noteRepository: NotyNoteRepository,
        private val notyTaskManager: NotyTaskManager,
    ) : BaseViewModel<AddNoteState>() {
        private val stateStore = StateStore(AddNoteState.initialState.mutable())

        override val state: StateFlow<AddNoteState> = stateStore.state

        private var job: Job? = null

        fun setTitle(title: String) {
            setState { this.title = title }
            validateNote()
        }

        fun setNote(note: String) {
            setState { this.note = note }
            validateNote()
        }

        fun add() {
            job?.cancel()
            job =
                viewModelScope.launch {
                    val title = state.value.title.trim()
                    val note = state.value.note.trim()

                    setState { isAdding = true }

                    val result = noteRepository.addNote(title, note)

                    result.onSuccess { noteId ->
                        scheduleNoteCreate(noteId)
                        setState {
                            isAdding = false
                            added = true
                        }
                    }.onFailure { message ->
                        setState {
                            isAdding = false
                            added = false
                            errorMessage = message
                        }
                    }
                }
        }

        private fun scheduleNoteCreate(noteId: String) = notyTaskManager.scheduleTask(NotyTask.create(noteId))

        private fun validateNote() {
            val isValid = NoteValidator.isValidNote(currentState.title, currentState.note)
            setState { showSave = isValid }
        }

        /**
         * In simpleapp module, ViewModel's instance is created using Hilt NavGraph ViewModel so it
         * doesn't clears the ViewModel when the Fragment's onDestroy() lifecycle is invoked and
         * thus it holds the stale state when the same fragment is relaunched. So this method is
         * simply a way for Fragment to ask ViewModel to reset the state.
         */
        fun resetState() {
            setState {
                note = ""
                title = ""
                note = ""
                showSave = false
                isAdding = false
                added = false
                errorMessage = null
            }
        }

        private fun setState(update: MutableAddNoteState.() -> Unit) = stateStore.setState(update)
    }
