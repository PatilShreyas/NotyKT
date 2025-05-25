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

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.store.StateStore
import dev.shreyaspatil.noty.utils.validator.AuthValidator
import dev.shreyaspatil.noty.view.state.LoginState
import dev.shreyaspatil.noty.view.state.MutableLoginState
import dev.shreyaspatil.noty.view.state.mutable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val notyUserRepository: NotyUserRepository,
        private val sessionManager: SessionManager,
    ) : BaseViewModel<LoginState>() {
        private val stateStore = StateStore(initialState = LoginState.initialState.mutable())

        override val state: StateFlow<LoginState> = stateStore.state

        fun setUsername(username: String) {
            setState { this.username = username }
        }

        fun setPassword(password: String) {
            setState { this.password = password }
        }

        fun login() {
            if (!validateCredentials()) return

            viewModelScope.launch {
                val username = currentState.username
                val password = currentState.password

                setState { isLoading = true }

                val response = notyUserRepository.getUserByUsernameAndPassword(username, password)

                response.onSuccess { authCredential ->
                    sessionManager.saveToken(authCredential.token)
                    setState {
                        isLoading = false
                        isLoggedIn = true
                        error = null
                    }
                }.onFailure { message ->
                    setState {
                        isLoading = false
                        isLoggedIn = false
                        error = message
                    }
                }
            }
        }

        fun clearError() = setState { error = null }

        private fun validateCredentials(): Boolean {
            val isValidUsername = AuthValidator.isValidUsername(currentState.username)
            val isValidPassword = AuthValidator.isValidPassword(currentState.password)

            setState {
                this.isValidUsername = isValidUsername
                this.isValidPassword = isValidPassword
            }

            return isValidUsername && isValidPassword
        }

        private fun setState(update: MutableLoginState.() -> Unit) = stateStore.setState(update)
    }
