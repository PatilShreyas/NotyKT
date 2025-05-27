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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.connectivity.ConnectionState.Available
import dev.shreyaspatil.noty.core.connectivity.ConnectivityObserver
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.task.NotyTaskManager
import dev.shreyaspatil.noty.core.task.TaskState
import dev.shreyaspatil.noty.di.LocalRepository
import dev.shreyaspatil.noty.store.StateStore
import dev.shreyaspatil.noty.view.state.MutableNotesState
import dev.shreyaspatil.noty.view.state.NotesState
import dev.shreyaspatil.noty.view.state.mutable
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel
    @Inject
    constructor(
        @LocalRepository private val notyNoteRepository: NotyNoteRepository,
        private val sessionManager: SessionManager,
        private val preferenceManager: PreferenceManager,
        private val notyTaskManager: NotyTaskManager,
        private val connectivityObserver: ConnectivityObserver,
    ) : BaseViewModel<NotesState>() {
        private val stateStore = StateStore(initialState = NotesState.initialState.mutable())

        override val state: StateFlow<NotesState> = stateStore.state

        private var syncJob: Job? = null

        init {
            checkUserSession()
            observeNotes()
            syncNotes()
            observeConnectivity()
        }

        fun syncNotes() {
            if (sessionManager.getToken() == null) return
            if (syncJob?.isActive == true) return

            syncJob =
                viewModelScope.launch {
                    val taskId = notyTaskManager.syncNotes()

                    try {
                        notyTaskManager.observeTask(taskId).collect { taskState ->
                            when (taskState) {
                                TaskState.SCHEDULED -> setState { isLoading = true }
                                TaskState.COMPLETED, TaskState.CANCELLED ->
                                    setState {
                                        isLoading = false
                                    }
                                TaskState.FAILED ->
                                    setState {
                                        isLoading = false
                                        error = "Failed to sync notes"
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Can't find work by ID '$taskId'")
                    }
                }
        }

        fun logout() {
            viewModelScope.launch {
                sessionManager.saveToken(null)
                notyTaskManager.abortAllTasks()
                notyNoteRepository.deleteAllNotes()
                setState { isUserLoggedIn = false }
            }
        }

        suspend fun isDarkModeEnabled() = preferenceManager.uiModeFlow.first()

        fun setDarkMode(enable: Boolean) {
            viewModelScope.launch {
                preferenceManager.setDarkMode(enable)
            }
        }

        private fun checkUserSession() {
            setState { isUserLoggedIn = sessionManager.getToken() != null }
        }

        private fun observeNotes() {
            notyNoteRepository.getAllNotes()
                .distinctUntilChanged()
                .onEach { response ->
                    response.onSuccess { notes ->
                        setState {
                            this.isLoading = false
                            this.notes = notes
                        }
                    }.onFailure { message ->
                        setState {
                            isLoading = false
                            error = message
                        }
                    }
                }.onStart { setState { isLoading = true } }
                .launchIn(viewModelScope)
        }

        private fun observeConnectivity() {
            connectivityObserver.connectionState
                .distinctUntilChanged()
                .map { it === Available }
                .onEach {
                    setState { isConnectivityAvailable = it }
                }
                .launchIn(viewModelScope)
        }

        private fun setState(update: MutableNotesState.() -> Unit) = stateStore.setState(update)

        companion object {
            private const val TAG = "NotesViewModel"
        }
    }
