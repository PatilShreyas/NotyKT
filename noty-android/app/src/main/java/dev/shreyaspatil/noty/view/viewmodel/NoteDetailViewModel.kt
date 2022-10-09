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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.utils.validator.NoteValidator
import dev.shreyaspatil.noty.view.state.NoteDetailState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class NoteDetailViewModel @AssistedInject constructor(
    private val notyTaskManager: NotyTaskManager,
    @LocalRepository private val noteRepository: NotyNoteRepository,
    @Assisted private val noteId: String
) : BaseViewModel<NoteDetailState>(initialState = NoteDetailState()) {

    private var job: Job? = null
    private lateinit var currentNote: Note

    init {
        loadNote()
    }

    fun setTitle(title: String) {
        setState { state -> state.copy(title = title) }
        validateNote()
    }

    fun setNote(note: String) {
        setState { state -> state.copy(note = note) }
        validateNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }
            val note = noteRepository.getNoteById(noteId).firstOrNull()
            if (note != null) {
                currentNote = note
                setState { state ->
                    state.copy(
                        isLoading = false,
                        title = note.title,
                        note = note.note,
                        isPinned = note.isPinned
                    )
                }
            } else {
                setState { state -> state.copy(isLoading = false, finished = true) }
            }
        }
    }

    fun save() {
        val title = currentState.title?.trim() ?: return
        val note = currentState.note?.trim() ?: return

        job?.cancel()
        job = viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }

            val response = noteRepository.updateNote(noteId, title, note)

            setState { state -> state.copy(isLoading = false) }

            response.onSuccess { noteId ->
                if (NotyNoteRepository.isTemporaryNote(noteId)) {
                    scheduleNoteCreate(noteId)
                } else {
                    scheduleNoteUpdate(noteId)
                }
                setState { state -> state.copy(finished = true) }
            }.onFailure { message ->
                setState { state -> state.copy(error = message) }
            }
        }
    }

    fun delete() {
        job?.cancel()
        job = viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }

            val response = noteRepository.deleteNote(noteId)

            setState { state -> state.copy(isLoading = false) }

            response.onSuccess { noteId ->
                if (!NotyNoteRepository.isTemporaryNote(noteId)) {
                    scheduleNoteDelete(noteId)
                }
                setState { state -> state.copy(finished = true) }
            }.onFailure { message ->
                setState { state -> state.copy(error = message) }
            }
        }
    }

    fun togglePin() {
        job?.cancel()
        job = viewModelScope.launch {
            setState { state -> state.copy(isLoading = true) }

            val response = noteRepository.pinNote(noteId, !currentState.isPinned)

            setState { state -> state.copy(isLoading = false, isPinned = !currentState.isPinned) }

            response.onSuccess { noteId ->
                if (!NotyNoteRepository.isTemporaryNote(noteId)) {
                    scheduleNoteUpdatePin(noteId)
                }
            }.onFailure { message ->
                setState { state -> state.copy(error = message) }
            }
        }
    }

    private fun validateNote() {
        try {
            val oldTitle = currentNote.title
            val oldNote = currentNote.note

            val title = currentState.title
            val note = currentState.note

            val isValid = title != null && note != null && NoteValidator.isValidNote(title, note)
            val areOldAndUpdatedNoteSame = oldTitle == title?.trim() && oldNote == note?.trim()

            setState { state -> state.copy(showSave = isValid && !areOldAndUpdatedNoteSame) }
        } catch (error: Throwable) {
        }
    }

    private fun scheduleNoteCreate(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.create(noteId))

    private fun scheduleNoteUpdate(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.update(noteId))

    private fun scheduleNoteDelete(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.delete(noteId))

    private fun scheduleNoteUpdatePin(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.pin(noteId))

    @AssistedFactory
    interface Factory {
        fun create(noteId: String): NoteDetailViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            noteId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return assistedFactory.create(noteId) as T
            }
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule
