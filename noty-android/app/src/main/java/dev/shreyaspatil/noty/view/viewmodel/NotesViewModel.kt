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
import androidx.lifecycle.*
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.di.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NotesViewModel @ViewModelInject constructor(
    @LocalRepository private val notyNoteRepository: NotyNoteRepository,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager,
    private val notyTaskManager: NotyTaskManager
) : ViewModel() {

    private var syncJob: Job? = null

    private val _syncState = MutableLiveData<ViewState<Unit>>()
    val syncState: LiveData<ViewState<Unit>> = _syncState

    val notes: LiveData<ViewState<List<Note>>> = notyNoteRepository.getAllNotes()
        .map { result ->
            when (result) {
                is ResponseResult.Success -> ViewState.success(result.data)
                is ResponseResult.Error -> ViewState.failed(result.message)
            }
        }.asLiveData()

    fun syncNotes() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            val taskId = notyTaskManager.syncNotes()

            notyTaskManager.observeTask(taskId).collect { taskState ->
                val viewState = when (taskState) {
                    TaskState.SCHEDULED -> ViewState.loading()
                    TaskState.COMPLETED, TaskState.CANCELLED -> ViewState.success(Unit)
                    TaskState.FAILED -> ViewState.failed("Failed")
                }

                _syncState.value = viewState
            }
        }
    }

    fun isUserLoggedIn() = sessionManager.getToken() != null

    fun clearUserSession() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionManager.saveToken(null)
            notyTaskManager.abortAllTasks()
            notyNoteRepository.deleteAllNotes()
        }
    }

    suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

    fun setDarkMode(enable: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enable)
        }
    }
}
