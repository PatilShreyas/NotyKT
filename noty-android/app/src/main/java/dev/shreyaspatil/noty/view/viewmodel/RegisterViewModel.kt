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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.repository.ResponseResult
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.core.view.ViewState
import dev.shreyaspatil.noty.utils.shareWhileObserved
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class RegisterViewModel @ViewModelInject constructor(
    private val notyUserRepository: NotyUserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _authFlow = MutableSharedFlow<ViewState<String>>()
    val authFlow: SharedFlow<ViewState<String>> = _authFlow.shareWhileObserved(viewModelScope)

    fun register(
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            _authFlow.emit(ViewState.loading())

            val responseState = notyUserRepository.addUser(username, password)

            val viewState = when (responseState) {
                is ResponseResult.Success -> {
                    val authCredential = responseState.data
                    saveToken(authCredential.token)
                    ViewState.success("Registration Successful")
                }

                is ResponseResult.Error -> ViewState.failed(responseState.message)
            }

            _authFlow.emit(viewState)
        }
    }

    private fun saveToken(token: String) {
        sessionManager.saveToken(token)
    }
}
