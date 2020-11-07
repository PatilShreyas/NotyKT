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
import dev.shreyaspatil.noty.core.model.Note
import dev.shreyaspatil.noty.core.preference.PreferenceManager
import dev.shreyaspatil.noty.core.repository.NotyNoteRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.view.ViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class NotesViewModel @ViewModelInject constructor(
    private val notyNoteRepository: NotyNoteRepository,
    private val sessionManager: SessionManager,
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    private var job: Job? = null

    private val _notesState = MutableLiveData<ViewState<List<Note>>>()

    val notesState: LiveData<ViewState<List<Note>>> = _notesState

    fun getAllNotes() {
        job?.cancel()

        job = viewModelScope.launch {
            notyNoteRepository.getAllNotes()
                .onStart { _notesState.value = ViewState.loading() }
                .collect {
                    val viewState = when (it) {
                        is ResponseResult.Success -> ViewState.success(it.data)
                        is ResponseResult.Error -> ViewState.failed(it.message)
                    }
                    _notesState.value = viewState
                }
        }
    }

    fun isUserLoggedIn() = sessionManager.getToken() != null

    fun clearUserSession() {
        sessionManager.saveToken(null)
        viewModelScope.launch {
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
