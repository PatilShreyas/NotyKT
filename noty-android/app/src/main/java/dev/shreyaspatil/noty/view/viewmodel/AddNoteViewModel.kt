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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.model.NotyTask
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.di.LocalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AddNoteViewModel @ViewModelInject constructor(
    @LocalRepository private val noteRepository: NotyNoteRepository,
    private val notyTaskManager: NotyTaskManager
) : ViewModel() {

    var job: Job? = null

    private val _addNoteState = MutableLiveData<ViewState<String>>()
    val addNoteState: LiveData<ViewState<String>> = _addNoteState

    fun addNote(title: String, note: String) {
        job?.cancel()
        job = viewModelScope.launch {
            _addNoteState.value = ViewState.loading()

            val state = when (val result = noteRepository.addNote(title, note)) {
                is ResponseResult.Success -> {
                    val noteId = result.data
                    scheduleNoteCreate(noteId)
                    ViewState.success(noteId)
                }
                is ResponseResult.Error -> ViewState.failed(result.message)
            }

            _addNoteState.value = state
        }
    }

    private fun scheduleNoteCreate(noteId: String) =
        notyTaskManager.scheduleTask(NotyTask.create(noteId))
}
