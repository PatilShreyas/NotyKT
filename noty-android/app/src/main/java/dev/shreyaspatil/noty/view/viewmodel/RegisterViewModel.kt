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
import dev.shreyaspatil.noty.utils.validator.AuthValidator
import dev.shreyaspatil.noty.view.state.RegisterState
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val notyUserRepository: NotyUserRepository,
    private val sessionManager: SessionManager
) : BaseViewModel<RegisterState>(initialState = RegisterState()) {

    fun setUsername(username: String) {
        setState { state -> state.copy(username = username) }
    }

    fun setPassword(password: String) {
        setState { state -> state.copy(password = password) }
    }

    fun setConfirmPassword(password: String) {
        setState { state -> state.copy(confirmPassword = password) }
    }

    fun register() {
        if (!validateCredentials()) return

        viewModelScope.launch {
            val username = currentState.username
            val password = currentState.password

            setState { state -> state.copy(isLoading = true) }

            val response = notyUserRepository.addUser(username, password)

            response.onSuccess { authCredential ->
                sessionManager.saveToken(authCredential.token)
                setState { state -> state.copy(isLoading = false, isLoggedIn = true, error = null) }
            }.onFailure { message ->
                setState { state ->
                    state.copy(isLoading = false, error = message, isLoggedIn = false)
                }
            }
        }
    }

    fun clearError() = setState { state -> state.copy(error = null) }

    private fun validateCredentials(): Boolean {
        val username = currentState.username
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword

        val isValidUsername = AuthValidator.isValidUsername(username)
        val isValidPassword = AuthValidator.isValidPassword(password)
        val arePasswordAndConfirmPasswordSame = AuthValidator.isPasswordAndConfirmPasswordSame(
            password,
            confirmPassword
        )

        setState { state ->
            state.copy(
                isValidUsername = isValidUsername,
                isValidPassword = isValidPassword,
                isValidConfirmPassword = arePasswordAndConfirmPasswordSame
            )
        }

        return isValidUsername && isValidPassword && arePasswordAndConfirmPasswordSame
    }
}
