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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.utils.shareWhileObserved
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class NotesViewModel @Inject constructor(
    @LocalRepository private val notyNoteRepository: NotyNoteRepository,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager,
    private val notyTaskManager: NotyTaskManager
) : ViewModel() {

    private var syncJob: Job? = null

    private val _syncState = MutableSharedFlow<ViewState<Unit>>()
    val syncState: SharedFlow<ViewState<Unit>> = _syncState.shareWhileObserved(viewModelScope)

    val notes: SharedFlow<ViewState<List<Note>>> = notyNoteRepository.getAllNotes()
        .distinctUntilChanged()
        .map { result ->
            when (result) {
                is ResponseResult.Success -> ViewState.success(result.data)
                is ResponseResult.Error -> ViewState.failed(result.message)
            }
        }.onStart { emit(ViewState.loading()) }
        .shareWhileObserved(viewModelScope)

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

                _syncState.emit(viewState)
            }
        }
    }

    fun isUserLoggedIn() = sessionManager.getToken() != null

    suspend fun clearUserSession() = withContext(Dispatchers.IO) {
        sessionManager.saveToken(null)
        notyTaskManager.abortAllTasks()
        notyNoteRepository.deleteAllNotes()
    }

    suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

    fun setDarkMode(enable: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enable)
        }
    }
}
