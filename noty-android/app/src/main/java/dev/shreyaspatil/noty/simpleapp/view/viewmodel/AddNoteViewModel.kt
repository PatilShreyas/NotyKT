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

package dev.shreyaspatil.noty.simpleapp.view.viewmodel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val notyNoteRepository: NotyNoteRepository
) : ViewModel() {

    var job: Job? = null

    private val _addNoteState = MutableLiveData<ViewState<String>>()
    val addNoteState: LiveData<ViewState<String>> = _addNoteState

    fun addNote(title: String, note: String) {
        job?.cancel()
        job = viewModelScope.launch {
            notyNoteRepository.addNote(title, note)
                .onStart { _addNoteState.value = ViewState.loading() }
                .collect { state ->
                    val viewState = when (state) {
                        is ResponseResult.Success -> ViewState.success<String>(state.data)
                        is ResponseResult.Error -> ViewState.failed<String>(state.message)
                    }
                    _addNoteState.value = viewState
                }
        }
    }
}
