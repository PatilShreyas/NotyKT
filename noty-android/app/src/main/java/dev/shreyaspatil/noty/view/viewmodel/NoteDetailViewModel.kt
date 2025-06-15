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
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.store.StateStore
import dev.shreyaspatil.noty.utils.validator.NoteValidator
import dev.shreyaspatil.noty.view.state.MutableNoteDetailState
import dev.shreyaspatil.noty.view.state.NoteDetailState
import dev.shreyaspatil.noty.view.state.mutable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = NoteDetailViewModel.Factory::class)
class NoteDetailViewModel
    @AssistedInject
    constructor(
        private val notyTaskManager: NotyTaskManager,
        @LocalRepository private val noteRepository: NotyNoteRepository,
        @Assisted private val noteId: String,
    ) : BaseViewModel<NoteDetailState>() {
        private val stateStore = StateStore(initialState = NoteDetailState.initialState.mutable())

        override val state: StateFlow<NoteDetailState> = stateStore.state

        private var job: Job? = null
        private lateinit var currentNote: Note

        init {
            loadNote()
        }

        fun setTitle(title: String) {
            setState { this.title = title }
            validateNote()
        }

        fun setNote(note: String) {
            setState { this.note = note }
            validateNote()
        }

        private fun loadNote() {
            viewModelScope.launch {
                setState { isLoading = true }
                val note = noteRepository.getNoteById(noteId).firstOrNull()
                if (note != null) {
                    currentNote = note
                    setState {
                        this.isLoading = false
                        this.title = note.title
                        this.note = note.note
                        this.isPinned = note.isPinned
                    }
                } else {
                    setState {
                        isLoading = false
                        finished = true
                    }
                }
            }
        }

        fun save() {
            val title = currentState.title?.trim() ?: return
            val note = currentState.note?.trim() ?: return

            job?.cancel()
            job =
                viewModelScope.launch {
                    setState { isLoading = true }

                    val response = noteRepository.updateNote(noteId, title, note)

                    setState { isLoading = false }

                    response.onSuccess { noteId ->
                        if (NotyNoteRepository.isTemporaryNote(noteId)) {
                            scheduleNoteCreate(noteId)
                        } else {
                            scheduleNoteUpdate(noteId)
                        }
                        setState { finished = true }
                    }.onFailure { message ->
                        setState { error = message }
                    }
                }
        }

        fun delete() {
            job?.cancel()
            job =
                viewModelScope.launch {
                    setState { isLoading = true }

                    val response = noteRepository.deleteNote(noteId)

                    setState { isLoading = false }

                    response.onSuccess { noteId ->
                        if (!NotyNoteRepository.isTemporaryNote(noteId)) {
                            scheduleNoteDelete(noteId)
                        }
                        setState { finished = true }
                    }.onFailure { message ->
                        setState { error = message }
                    }
                }
        }

        fun togglePin() {
            job?.cancel()
            job =
                viewModelScope.launch {
                    setState { isLoading = true }

                    val response = noteRepository.pinNote(noteId, !currentState.isPinned)

                    setState {
                        isLoading = false
                        isPinned = !currentState.isPinned
                    }

                    response.onSuccess { noteId ->
                        if (!NotyNoteRepository.isTemporaryNote(noteId)) {
                            scheduleNoteUpdatePin(noteId)
                        }
                    }.onFailure { message ->
                        setState { error = message }
                    }
                }
        }

        private fun validateNote() {
            try {
                val oldTitle = currentNote.title
                val oldNote = currentNote.note

                val title = currentState.title
                val note = currentState.note

                val isValid =
                    title != null &&
                        note != null &&
                        NoteValidator.isValidNote(title, note)

                val areOldAndUpdatedNoteSame = oldTitle == title?.trim() && oldNote == note?.trim()

                setState { showSave = isValid && !areOldAndUpdatedNoteSame }
            } catch (error: Throwable) {
            }
        }

        private fun scheduleNoteCreate(noteId: String) = notyTaskManager.scheduleTask(NotyTask.create(noteId))

        private fun scheduleNoteUpdate(noteId: String) = notyTaskManager.scheduleTask(NotyTask.update(noteId))

        private fun scheduleNoteDelete(noteId: String) = notyTaskManager.scheduleTask(NotyTask.delete(noteId))

        private fun scheduleNoteUpdatePin(noteId: String) = notyTaskManager.scheduleTask(NotyTask.pin(noteId))

        private fun setState(update: MutableNoteDetailState.() -> Unit) {
            stateStore.setState(update)
        }

        @AssistedFactory
        interface Factory {
            fun create(noteId: String): NoteDetailViewModel
        }
    }
