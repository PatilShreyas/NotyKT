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

import dev.shreyaspatil.noty.base.ViewModelTest
import dev.shreyaspatil.noty.core.model.AuthCredential
import dev.shreyaspatil.noty.core.repository.Either
import dev.shreyaspatil.noty.core.repository.NotyUserRepository
import dev.shreyaspatil.noty.core.session.SessionManager
import dev.shreyaspatil.noty.view.state.LoginState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest : ViewModelTest() {
    private lateinit var repository: NotyUserRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: LoginViewModel

    @BeforeEach
    fun setup() {
        repository = mockk()
        sessionManager = mockk(relaxUnitFun = true)
        viewModel = LoginViewModel(repository, sessionManager)
    }

    @Test
    fun `initial state should be valid`() {
        val expectedState =
            LoginState(
                isLoading = false,
                isLoggedIn = false,
                error = null,
                username = "",
                password = "",
                isValidUsername = null,
                isValidPassword = null,
            )
        viewModel currentStateShouldBe expectedState
    }

    @Test
    fun `username should be updated in the current state when set`() {
        // Given
        val username = "johndoe"

        // When
        viewModel.setUsername(username)

        // Then
        viewModel.withState { assertEquals(username, this.username) }
    }

    @Test
    fun `password should be updated in the current state when set`() {
        // Given
        val password = "eodnhoj"

        // When
        viewModel.setPassword(password)

        // Then
        viewModel.withState { assertEquals(password, this.password) }
    }

    @Test
    fun `login should validate credentials and update state when credentials are incomplete`() =
        runTest {
            // Given
            val username = "john"
            val password = "eod"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            // When
            viewModel.login()

            // Then
            viewModel.withState {
                assertTrue(isValidUsername!!)
                assertFalse(isValidPassword!!)
            }

            coVerify(exactly = 0) {
                repository.getUserByUsernameAndPassword(username, password)
            }
        }

    @Test
    fun `login should authenticate user and update state when credentials are valid`() =
        runTest {
            // Given
            val username = "johndoe1234"
            val password = "4321eodnhoj"
            val token = "Bearer TOKEN_ABC"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.success(AuthCredential(token)))

            // When
            viewModel.login()

            // Then
            coVerify { repository.getUserByUsernameAndPassword(username, password) }
            verify { sessionManager.saveToken(eq(token)) }

            viewModel.withState {
                assertTrue(isValidUsername!!)
                assertTrue(isValidPassword!!)
                assertFalse(isLoading)
                assertTrue(isLoggedIn)
                assertNull(error)
            }
        }

    @Test
    fun `login should update state with error when repository fails`() =
        runTest {
            // Given
            val username = "johndoe12345"
            val password = "54321eodnhoj"

            viewModel.setUsername(username)
            viewModel.setPassword(password)

            coEvery { repository.getUserByUsernameAndPassword(username, password) }
                .returns(Either.error("User not exist"))

            // When
            viewModel.login()

            // Then
            coVerify { repository.getUserByUsernameAndPassword(username, password) }

            viewModel.withState {
                assertFalse(isLoggedIn)
                assertEquals("User not exist", error)
            }
        }
}
