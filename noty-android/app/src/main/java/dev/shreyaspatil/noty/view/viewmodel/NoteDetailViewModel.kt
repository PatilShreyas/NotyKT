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
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.utils.shareWhileObserved
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NoteDetailViewModel @AssistedInject constructor(
    private val notyTaskManager: NotyTaskManager,
    @LocalRepository private val noteRepository: NotyNoteRepository,
    @Assisted private val noteId: String
) : ViewModel() {

    init {
        viewModelScope.launch {
            noteRepository.getNoteById(noteId).firstOrNull()
                ?.let { _note.emit(it) }
        }
    }

    private var job: Job? = null

    private val _note = MutableSharedFlow<Note>()
    val note: SharedFlow<Note> = _note.shareWhileObserved(viewModelScope)

    private val _updateNoteState = MutableSharedFlow<ViewState<Unit>>()
    val updateNoteState = _updateNoteState.shareWhileObserved(viewModelScope)

    private val _deleteNoteState = MutableSharedFlow<ViewState<Unit>>()
    val deleteNoteState = _deleteNoteState.shareWhileObserved(viewModelScope)

    fun updateNote(title: String, note: String) {
        job?.cancel()
        job = viewModelScope.launch {
            _updateNoteState.emit(ViewState.loading())

            val viewState = when (val result = noteRepository.updateNote(noteId, title, note)) {
                is ResponseResult.Success -> {
                    val noteId = result.data

                    if (NotyNoteRepository.isTemporaryNote(noteId)) {
                        scheduleNoteCreate(noteId)
                    } else {
                        scheduleNoteUpdate(noteId)
                    }

                    ViewState.success(Unit)
                }
                is ResponseResult.Error -> ViewState.failed(result.message)
            }

            _updateNoteState.emit(viewState)
        }
    }

    fun deleteNote() {
        job?.cancel()
        job = viewModelScope.launch {
            _updateNoteState.emit(ViewState.loading())

            val viewState = when (val result = noteRepository.deleteNote(noteId)) {
                is ResponseResult.Success -> {
                    val noteId = result.data

                    if (!NotyNoteRepository.isTemporaryNote(noteId)) {
                        scheduleNoteDelete(noteId)
                    }
                    ViewState.success(Unit)
                }
                is ResponseResult.Error -> ViewState.failed(result.message)
            }
            _deleteNoteState.emit(viewState)
        }
    }

    private fun scheduleNoteCreate(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.create(noteId))

    private fun scheduleNoteUpdate(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.update(noteId))

    private fun scheduleNoteDelete(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.delete(noteId))

    @AssistedFactory
    interface Factory {
        fun create(noteId: String): NoteDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: Factory,
            noteId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(noteId) as T
            }
        }
    }
}

@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule
