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

import android.util.Log
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
import dev.shreyaspatil.noty.core.ui.UIDataState
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.utils.ext.shareWhileObserved
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    @LocalRepository private val notyNoteRepository: NotyNoteRepository,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager,
    private val notyTaskManager: NotyTaskManager,
) : ViewModel() {

    private var syncJob: Job? = null

    private val _syncState = MutableSharedFlow<UIDataState<Unit>>()
    val syncState: SharedFlow<UIDataState<Unit>> = _syncState.shareWhileObserved(viewModelScope)

    private val _loggedInState = MutableStateFlow(isUserLoggedIn())
    val userLoggedInState: StateFlow<Boolean> = _loggedInState

    val notes: SharedFlow<UIDataState<List<Note>>> = notyNoteRepository.getAllNotes()
        .distinctUntilChanged()
        .map { result ->
            when (result) {
                is ResponseResult.Success -> UIDataState.success(result.data)
                is ResponseResult.Error -> UIDataState.failed(result.message)
            }
        }.onStart { emit(UIDataState.loading()) }
        .shareWhileObserved(viewModelScope)

    fun syncNotes() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            val taskId = notyTaskManager.syncNotes()

            try {
                notyTaskManager.observeTask(taskId).collect { taskState ->
                    val viewState = when (taskState) {
                        TaskState.SCHEDULED -> UIDataState.loading()
                        TaskState.COMPLETED, TaskState.CANCELLED -> UIDataState.success(Unit)
                        TaskState.FAILED -> UIDataState.failed("Failed")
                    }

                    _syncState.emit(viewState)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Can't find work by ID '$taskId'")
            }
        }
    }

    private fun isUserLoggedIn() = sessionManager.getToken() != null

    fun clearUserSession() {
        viewModelScope.launch {
            sessionManager.saveToken(null)
            notyTaskManager.abortAllTasks()
            notyNoteRepository.deleteAllNotes()
            _loggedInState.value = false
        }
    }

    suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

    fun setDarkMode(enable: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enable)
        }
    }

    companion object {
        const val TAG = "NotesViewModel"
    }
}
