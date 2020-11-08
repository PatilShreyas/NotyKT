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

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.view.ViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AddNoteViewModel @ViewModelInject constructor(
    private val notyNoteRepository: NotyNoteRepository,
    @Assisted state: SavedStateHandle
) : ViewModel() {

    var job: Job? = null
    private val _addNoteState = MutableLiveData<ViewState<String>>()

    private val _currentNote: MutableLiveData<Pair<String, String>> =
        state.getLiveData(RESTORED_NOTE)

    val addNoteState = _currentNote.switchMap { note ->
        job?.cancel()

        job = viewModelScope.launch {
            notyNoteRepository.addNote(note.first, note.second)
                .onStart {
                    _addNoteState.value = ViewState.loading()
                }
                .collect { state ->
                    val viewState = when (state) {
                        is ResponseResult.Success -> ViewState.success(
                            state.data
                        )
                        is ResponseResult.Error -> ViewState.failed(
                            state.message
                        )
                    }
                    _addNoteState.value = viewState
                }

        }
        _addNoteState
    }

    fun addNote(title: String, note: String) {
        _currentNote.value = Pair(title, note)
    }

    companion object {
        private const val RESTORED_NOTE = "RESTORED_NOTE"
    }
}
