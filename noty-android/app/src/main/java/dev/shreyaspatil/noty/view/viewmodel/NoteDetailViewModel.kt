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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.assisted.dagger2.AssistedModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.view.ViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NoteDetailViewModel @AssistedInject constructor(
    private val notyNoteRepository: NotyNoteRepository,
    @Assisted private val noteId: String
) : ViewModel() {

    init {
        viewModelScope.launch {
            notyNoteRepository.getNoteById(noteId).first()
                .let { _noteLiveData.value = it }
        }
    }

    private var job: Job? = null

    private val _noteLiveData = MutableLiveData<Note>()
    val noteLiveData: LiveData<Note> = _noteLiveData

    private val _updateNoteState = MutableLiveData<ViewState<Unit>>()
    val updateNoteState: LiveData<ViewState<Unit>> = _updateNoteState

    private val _deleteNoteState = MutableLiveData<ViewState<Unit>>()
    val deleteNoteState: LiveData<ViewState<Unit>> = _deleteNoteState

    fun updateNote(title: String, note: String) {
        job?.cancel()
        job = viewModelScope.launch {
            notyNoteRepository.updateNote(noteId, title, note)
                .onStart { _updateNoteState.value = ViewState.loading() }
                .collect { state ->
                    val viewState = when (state) {
                        is ResponseResult.Success -> ViewState.success(Unit)
                        is ResponseResult.Error -> ViewState.failed(state.message)
                    }
                    _updateNoteState.value = viewState
                }
        }
    }

    fun deleteNote() {
        job?.cancel()
        job = viewModelScope.launch {
            notyNoteRepository.deleteNote(noteId)
                .onStart { _updateNoteState.value = ViewState.loading() }
                .collect { state ->
                    val viewState = when (state) {
                        is ResponseResult.Success -> ViewState.success(Unit)
                        is ResponseResult.Error -> ViewState.failed(state.message)
                    }
                    _deleteNoteState.value = viewState
                }
        }
    }

    @AssistedInject.Factory
    interface AssistedFactory {
        fun create(noteId: String): NoteDetailViewModel
    }

    companion object {
        fun provideFactory(
            assistedFactory: AssistedFactory,
            noteId: String
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return assistedFactory.create(noteId) as T
            }
        }
    }
}

@AssistedModule
@Module
@InstallIn(FragmentComponent::class)
interface AssistedInjectModule
