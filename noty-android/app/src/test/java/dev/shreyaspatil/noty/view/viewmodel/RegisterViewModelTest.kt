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
import dev.shreyaspatil.noty.view.state.RegisterState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RegisterViewModelTest : ViewModelTest() {
    private lateinit var repository: NotyUserRepository
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: RegisterViewModel

    @BeforeEach
    fun setup() {
        repository = mockk()
        sessionManager = mockk(relaxUnitFun = true)
        viewModel = RegisterViewModel(repository, sessionManager)
    }

    @Test
    fun `initial state should be valid`() {
        // Given
        val expectedState =
            RegisterState(
                isLoading = false,
                isLoggedIn = false,
                error = null,
                username = "",
                password = "",
                confirmPassword = "",
                isValidUsername = null,
                isValidPassword = null,
                isValidConfirmPassword = null,
            )

        // Then
        assertEquals(expectedState, viewModel.currentState)
    }

    @Test
    fun `setUsername should update username in current state`() {
        // Given
        val username = "johndoe"

        // When
        viewModel.setUsername(username)

        // Then
        assertEquals(username, viewModel.currentState.username)
    }

    @Test
    fun `setPassword should update password in current state`() {
        // Given
        val password = "eodnhoj"

        // When
        viewModel.setPassword(password)

        // Then
        assertEquals(password, viewModel.currentState.password)
    }

    @Test
    fun `setConfirmPassword should update confirmPassword in current state`() {
        // Given
        val confirmPassword = "eodnhojabcd"

        // When
        viewModel.setConfirmPassword(confirmPassword)

        // Then
        assertEquals(confirmPassword, viewModel.currentState.confirmPassword)
    }

    @Test
    fun `register should not create user and update state when credentials are incomplete`() {
        // Given
        val username = "joh"
        val password = "doe"
        val confirmPassword = ""

        viewModel.setUsername(username)
        viewModel.setPassword(password)
        viewModel.setConfirmPassword(confirmPassword)

        // When
        viewModel.register()

        // Then
        coVerify(exactly = 0) { repository.addUser(username, password) }

        assertFalse(viewModel.currentState.isValidUsername!!)
        assertFalse(viewModel.currentState.isValidPassword!!)
        assertFalse(viewModel.currentState.isValidConfirmPassword!!)
    }

    @Test
    fun `register should update state with error when repository fails`() =
        runTest {
            // Given
            val username = "john"
            val password = "doe12345"

            viewModel.setUsername(username)
            viewModel.setPassword(password)
            viewModel.setConfirmPassword(password)

            coEvery { repository.addUser(username, password) }
                .returns(Either.error("Invalid credentials"))

            // When
            viewModel.register()

            // Then
            coVerify { repository.addUser(username, password) }
            assertEquals("Invalid credentials", viewModel.currentState.error)
        }

    @Test
    fun `register should save token and update state when credentials are valid`() =
        runTest {
            // Given
            val username = "johndoe"
            val password = "eodnhoj1234"
            val token = "Bearer TOKEN_ABC"

            viewModel.setUsername(username)
            viewModel.setPassword(password)
            viewModel.setConfirmPassword(password)

            coEvery { repository.addUser(username, password) }
                .returns(Either.success(AuthCredential(token)))

            // When
            viewModel.register()

            // Then
            coVerify { repository.addUser(username, password) }
            verify { sessionManager.saveToken(eq(token)) }

            assertTrue(viewModel.currentState.isLoggedIn)
            assertNull(viewModel.currentState.error)
        }
}
